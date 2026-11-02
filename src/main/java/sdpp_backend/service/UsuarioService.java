package sdpp_backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.UsuarioRequest;
import sdpp_backend.dto.CadastroDTOs.UsuarioResponse;
import sdpp_backend.exception.ConflitoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.UsuarioModel;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.UsuarioRepository;
import sdpp_backend.security.AcessoService;

/** Contas de acesso, gerenciadas pelo RH (PA-14). Senhas sempre com BCrypt (RNF-SEG-02). */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final ColaboradorRepository colaboradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcessoService acesso;

    public UsuarioService(UsuarioRepository repository, ColaboradorRepository colaboradorRepository,
                          PasswordEncoder passwordEncoder, AcessoService acesso) {
        this.repository = repository;
        this.colaboradorRepository = colaboradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return repository.findAll().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return paraResponse(buscar(id));
    }

    @Transactional
    public UsuarioResponse salvar(UsuarioRequest req) {
        if (req.senha() == null || req.senha().isBlank()) {
            throw new RegraNegocioException("Informe a senha inicial do usuário.");
        }
        String email = req.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário com este e-mail.");
        }
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setPerfil(req.perfil());
        usuario.setAtivo(req.ativo() == null || req.ativo());
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario = repository.save(usuario);
        vincular(usuario, req.colaboradorId());
        return paraResponse(usuario);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req) {
        UsuarioModel usuario = buscar(id);
        String email = req.email().trim().toLowerCase();
        if (!usuario.getEmail().equalsIgnoreCase(email) && repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário com este e-mail.");
        }
        boolean ativo = req.ativo() == null || req.ativo();
        if (usuario.getId().equals(acesso.atual().usuarioId()) && (!ativo || usuario.getPerfil() != req.perfil())) {
            throw new RegraNegocioException("Você não pode desativar nem alterar o perfil da sua própria conta.");
        }
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setPerfil(req.perfil());
        usuario.setAtivo(ativo);
        if (req.senha() != null && !req.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(req.senha()));
        }
        colaboradorRepository.findByUsuarioId(usuario.getId()).ifPresent(c -> {
            if (!c.getId().equals(req.colaboradorId())) {
                c.setUsuario(null);
                colaboradorRepository.saveAndFlush(c);
            }
        });
        vincular(usuario, req.colaboradorId());
        return paraResponse(usuario);
    }

    /** Exclusão lógica: a conta é desativada e o histórico é preservado. */
    @Transactional
    public void desativar(Long id) {
        UsuarioModel usuario = buscar(id);
        if (usuario.getId().equals(acesso.atual().usuarioId())) {
            throw new RegraNegocioException("Você não pode desativar a sua própria conta.");
        }
        usuario.setAtivo(false);
    }

    private void vincular(UsuarioModel usuario, Long colaboradorId) {
        if (colaboradorId == null) {
            return;
        }
        ColaboradorModel colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
        if (colaborador.getUsuario() != null && !colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ConflitoException("Este colaborador já possui uma conta de acesso.");
        }
        colaborador.setUsuario(usuario);
    }

    private UsuarioModel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private UsuarioResponse paraResponse(UsuarioModel u) {
        return UsuarioResponse.de(u, colaboradorRepository.findByUsuarioId(u.getId()).orElse(null));
    }
}
