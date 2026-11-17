package sdpp_backend.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.ColaboradorRequest;
import sdpp_backend.dto.CadastroDTOs.ColaboradorResponse;
import sdpp_backend.exception.ConflitoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.UsuarioModel;
import sdpp_backend.model.enums.Perfil;
import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.TipoAvaliacao;
import sdpp_backend.repository.AvaliacaoRepository;
import sdpp_backend.repository.CargoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.DepartamentoRepository;
import sdpp_backend.repository.PontuacaoRepository;
import sdpp_backend.repository.UsuarioRepository;
import sdpp_backend.security.AcessoService;

/** Cadastro de colaboradores (RF-COL-01, RF-COL-02). Escrita só pelo RH; leitura conforme a equipe. */
@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PontuacaoRepository pontuacaoRepository;
    private final AcessoService acesso;

    public ColaboradorService(ColaboradorRepository repository, CargoRepository cargoRepository,
                              DepartamentoRepository departamentoRepository, UsuarioRepository usuarioRepository,
                              AvaliacaoRepository avaliacaoRepository, PontuacaoRepository pontuacaoRepository,
                              AcessoService acesso) {
        this.repository = repository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.pontuacaoRepository = pontuacaoRepository;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return acesso.colaboradoresVisiveis(acesso.atual()).stream().map(ColaboradorResponse::de).toList();
    }

    /** Lista simplificada para escolher o gestor responsável no formulário do RH. */
    @Transactional(readOnly = true)
    public List<ColaboradorResponse> equipe() {
        return acesso.equipe(acesso.atual()).stream().map(ColaboradorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse buscarPorId(Long id) {
        ColaboradorModel c = buscar(id);
        acesso.exigirPodeVer(acesso.atual(), c);
        return ColaboradorResponse.de(c);
    }

    @Transactional
    public ColaboradorResponse salvar(ColaboradorRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um colaborador com este e-mail.");
        }
        ColaboradorModel c = new ColaboradorModel();
        preencher(c, req, email);
        return ColaboradorResponse.de(repository.save(c));
    }

    @Transactional
    public ColaboradorResponse atualizar(Long id, ColaboradorRequest req) {
        ColaboradorModel c = buscar(id);
        String email = req.email().trim().toLowerCase();
        if (!c.getEmail().equalsIgnoreCase(email) && repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um colaborador com este e-mail.");
        }
        sincronizarEmailDoAcesso(c, email);
        preencher(c, req, email);
        return ColaboradorResponse.de(c);
    }

    /**
     * Exclusão lógica: o colaborador é desativado para preservar avaliações, PDIs e histórico.
     * A conta de acesso vinculada também é desativada.
     */
    @Transactional
    public void desativar(Long id) {
        ColaboradorModel c = buscar(id);
        exigirSemEquipeAtiva(c, "desativar");
        c.setAtivo(false);
        if (c.getUsuario() != null) {
            c.getUsuario().setAtivo(false);
        }
    }

    private void preencher(ColaboradorModel c, ColaboradorRequest req, String email) {
        boolean ativo = req.ativo() == null || req.ativo();
        if (c.getId() != null && Boolean.TRUE.equals(c.getAtivo()) && !ativo) {
            exigirSemEquipeAtiva(c, "desativar");
        }
        Long gestorAnterior = c.getGestor() == null ? null : c.getGestor().getId();
        c.setNome(req.nome().trim());
        c.setEmail(email);
        c.setDataAdmissao(req.dataAdmissao());
        c.setAtivo(ativo);
        c.setCargo(cargoRepository.findById(req.cargoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cargo não encontrado.")));
        c.setDepartamento(departamentoRepository.findById(req.departamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Departamento não encontrado.")));
        c.setGestor(req.gestorId() == null ? null : validarGestor(c, req.gestorId()));
        if (c.getId() != null && !Objects.equals(gestorAnterior, req.gestorId())) {
            descartarAvaliacoesDoGestorEmAndamento(c);
        }
        // Colaborador inativo não acessa o sistema
        if (!c.getAtivo() && c.getUsuario() != null) {
            c.getUsuario().setAtivo(false);
        }
    }

    /**
     * O gestor não pode ser o próprio colaborador nem gerar ciclo na hierarquia, e precisa poder gerir a equipe:
     * ativo e com acesso de perfil Gestor ou RH (um perfil Colaborador não avalia nem acompanha a equipe).
     * As exigências de acesso valem só quando o gestor muda, para não travar edições de cadastros antigos.
     */
    private ColaboradorModel validarGestor(ColaboradorModel c, Long gestorId) {
        ColaboradorModel gestor = repository.findById(gestorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gestor não encontrado."));
        boolean gestorAlterado = c.getGestor() == null || !c.getGestor().getId().equals(gestorId);
        if (gestorAlterado) {
            if (!Boolean.TRUE.equals(gestor.getAtivo())) {
                throw new RegraNegocioException("O gestor responsável precisa estar ativo.");
            }
            UsuarioModel conta = gestor.getUsuario();
            if (conta == null || !Boolean.TRUE.equals(conta.getAtivo())
                    || (conta.getPerfil() != Perfil.GESTOR && conta.getPerfil() != Perfil.RH)) {
                throw new RegraNegocioException(gestor.getNome() + " não pode ser gestor(a) responsável: é preciso ter"
                        + " acesso ativo com perfil Gestor ou RH (ajuste em Usuários e acessos).");
            }
        }
        for (ColaboradorModel g = gestor; g != null; g = g.getGestor()) {
            if (c.getId() != null && c.getId().equals(g.getId())) {
                throw new RegraNegocioException("Gestor inválido: criaria um ciclo na hierarquia.");
            }
        }
        return gestor;
    }

    /** Quem gere uma equipe ativa não pode ser desativado: a equipe ficaria sem ninguém para avaliá-la. */
    private void exigirSemEquipeAtiva(ColaboradorModel c, String acao) {
        long equipe = repository.countByGestorIdAndAtivoTrue(c.getId());
        if (equipe > 0) {
            throw new RegraNegocioException("Não é possível " + acao + " " + c.getNome() + ": é gestor(a) responsável de "
                    + equipe + " colaborador(es) ativo(s). Defina outro gestor para a equipe antes.");
        }
    }

    /**
     * Troca de gestor durante um ciclo: a avaliação do gestor ainda em rascunho pertencia ao gestor anterior
     * (o avaliador fica gravado nela) e travaria o novo gestor. Ela é descartada para que o novo gestor
     * avalie do zero; avaliações já concluídas são preservadas.
     */
    private void descartarAvaliacoesDoGestorEmAndamento(ColaboradorModel c) {
        for (AvaliacaoModel a : avaliacaoRepository.findByColaboradorId(c.getId())) {
            if (a.getTipo() == TipoAvaliacao.GESTOR && a.getStatus() == StatusAvaliacao.EM_ANDAMENTO
                    && a.getCiclo().getStatus() == StatusCiclo.LIBERADO) {
                pontuacaoRepository.deleteByAvaliacaoId(a.getId());
                avaliacaoRepository.delete(a);
            }
        }
    }

    /** O e-mail de acesso acompanha o do cadastro, para não divergirem. */
    private void sincronizarEmailDoAcesso(ColaboradorModel c, String email) {
        UsuarioModel conta = c.getUsuario();
        if (conta == null || conta.getEmail().equalsIgnoreCase(email)) {
            return;
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe uma conta de acesso com este e-mail.");
        }
        conta.setEmail(email);
    }

    private ColaboradorModel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
    }
}
