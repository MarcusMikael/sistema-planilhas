package sdpp_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.LoginRequest;
import sdpp_backend.dto.CadastroDTOs.LoginResponse;
import sdpp_backend.dto.CadastroDTOs.TrocarSenhaRequest;
import sdpp_backend.dto.CadastroDTOs.UsuarioAtualResponse;
import sdpp_backend.exception.CredenciaisInvalidasException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.UsuarioModel;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.UsuarioRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.JwtService;
import sdpp_backend.security.UsuarioLogado;

/** Autenticação (RF-SEG-01, CA-RF-SEG-01). */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AcessoService acesso;

    public AuthService(UsuarioRepository usuarioRepository, ColaboradorRepository colaboradorRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, AcessoService acesso) {
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        UsuarioModel usuario = usuarioRepository.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenha()))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos."));
        Long colaboradorId = colaboradorRepository.findByUsuarioId(usuario.getId()).map(c -> c.getId()).orElse(null);
        var logado = new UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), colaboradorId);
        return new LoginResponse(jwtService.gerar(usuario.getId()), paraResponse(logado));
    }

    @Transactional(readOnly = true)
    public UsuarioAtualResponse atual() {
        return paraResponse(acesso.atual());
    }

    @Transactional
    public void trocarSenha(TrocarSenhaRequest req) {
        UsuarioModel usuario = usuarioRepository.findById(acesso.atual().usuarioId()).orElseThrow();
        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        usuario.setSenha(passwordEncoder.encode(req.novaSenha()));
    }

    private UsuarioAtualResponse paraResponse(UsuarioLogado u) {
        boolean temEquipe = u.colaboradorId() != null && colaboradorRepository.existsByGestorId(u.colaboradorId());
        return new UsuarioAtualResponse(u.usuarioId(), u.nome(), u.email(), u.perfil(), u.colaboradorId(), temEquipe);
    }
}
