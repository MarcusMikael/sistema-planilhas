package sdpp_backend.security;

import java.io.IOException;
import java.util.List;

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

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository,
                         ColaboradorRepository colaboradorRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long usuarioId = jwtService.validar(header.substring(7));
            if (usuarioId != null) {
                usuarioRepository.findById(usuarioId)
                        .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
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
