package sdpp_backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.UsuarioRepository;

/** Lê o cabeçalho "Authorization: Bearer <token>" e autentica a requisição. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final CookieSessao cookieSessao;

    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS");

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository,
                         ColaboradorRepository colaboradorRepository, CookieSessao cookieSessao) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.cookieSessao = cookieSessao;
    }

    /**
     * Token do cabeçalho Authorization (clientes de API) ou do cookie HttpOnly (navegador).
     * Pelo cookie, requisições que alteram dados exigem o cabeçalho X-SDPP-CSRF: o navegador envia cookies
     * em requisições disparadas por outros sites, mas um site de terceiros não consegue adicionar esse
     * cabeçalho sem a liberação do CORS. Sem ele, a requisição segue sem autenticação.
     */
    private Optional<String> token(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        boolean seguro = METODOS_SEGUROS.contains(request.getMethod());
        if (!seguro && request.getHeader(CookieSessao.CABECALHO_CSRF) == null) {
            return Optional.empty();
        }
        return cookieSessao.ler(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<String> bruto = token(request);
        if (bruto.isPresent()) {
            JwtService.TokenValido token = jwtService.validar(bruto.get());
            if (token != null) {
                usuarioRepository.findById(token.usuarioId())
                        .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                        // Tokens emitidos antes da última troca de senha são recusados
                        .filter(u -> u.getVersaoToken() == token.versao())
                        .ifPresent(u -> {
                            Long colaboradorId = colaboradorRepository.findByUsuarioId(u.getId())
                                    .map(c -> c.getId()).orElse(null);
                            UsuarioLogado logado = new UsuarioLogado(
                                    u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), colaboradorId);
                            var auth = new UsernamePasswordAuthenticationToken(logado, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getPerfil().name())));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        chain.doFilter(request, response);
    }
}
