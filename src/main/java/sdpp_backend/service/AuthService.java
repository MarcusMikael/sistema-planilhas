package sdpp_backend.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.LoginRequest;
import sdpp_backend.dto.CadastroDTOs.LoginResponse;
import sdpp_backend.dto.CadastroDTOs.TokenResponse;
import sdpp_backend.dto.CadastroDTOs.TrocarSenhaRequest;
import sdpp_backend.dto.CadastroDTOs.UsuarioAtualResponse;
import sdpp_backend.exception.CredenciaisInvalidasException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.UsuarioModel;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.UsuarioRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.JwtService;
import sdpp_backend.security.LimiteTentativasLogin;
import sdpp_backend.security.UsuarioLogado;

/** Autenticação (RF-SEG-01, CA-RF-SEG-01). */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AcessoService acesso;
    private final LimiteTentativasLogin limite;
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, ColaboradorRepository colaboradorRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, AcessoService acesso,
                       LimiteTentativasLogin limite) {
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.acesso = acesso;
        this.limite = limite;
        this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * @param dispositivo id do navegador confiável para este e-mail (cookie de dispositivo), ou null
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req, String dispositivo) {
        String email = req.email().trim();
        limite.verificar(email, dispositivo);
        UsuarioModel usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        // Sem conta, compara com um hash fictício para o tempo de resposta não revelar quais e-mails existem
        boolean senhaOk = passwordEncoder.matches(req.senha(), usuario == null ? hashFicticio : usuario.getSenha());
        if (usuario == null || !senhaOk || !Boolean.TRUE.equals(usuario.getAtivo())) {
            limite.registrarFalha(email, dispositivo);
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos.");
        }
        limite.registrarSucesso(email, dispositivo);
        Long colaboradorId = colaboradorRepository.findByUsuarioId(usuario.getId()).map(c -> c.getId()).orElse(null);
        var logado = new UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), colaboradorId);
        return new LoginResponse(jwtService.gerar(usuario.getId(), usuario.getVersaoToken()), paraResponse(logado));
    }

    @Transactional(readOnly = true)
    public UsuarioAtualResponse atual() {
        return paraResponse(acesso.atual());
    }

    /** Troca a senha, invalida as sessões abertas em outros dispositivos e devolve um token novo. */
    @Transactional
    public TokenResponse trocarSenha(TrocarSenhaRequest req) {
        UsuarioModel usuario = usuarioRepository.findById(acesso.atual().usuarioId()).orElseThrow();
        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        usuario.setSenha(passwordEncoder.encode(req.novaSenha()));
        usuario.revogarTokens();
        return new TokenResponse(jwtService.gerar(usuario.getId(), usuario.getVersaoToken()));
    }

    private UsuarioAtualResponse paraResponse(UsuarioLogado u) {
        // Só conta como "tem equipe" quem pode geri-la (perfil Gestor ou RH)
        boolean temEquipe = u.podeGerirEquipe() && u.colaboradorId() != null
                && colaboradorRepository.existsByGestorId(u.colaboradorId());
        return new UsuarioAtualResponse(u.usuarioId(), u.nome(), u.email(), u.perfil(), u.colaboradorId(), temEquipe);
    }
}
