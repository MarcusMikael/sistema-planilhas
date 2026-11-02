package sdpp_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.DesenvolvimentoDTOs.AcaoRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.AcaoResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.PDIRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.PDIResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.ProgressoRequest;
import sdpp_backend.exception.AcessoNegadoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.AcaoPDIModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.PDIModel;
import sdpp_backend.model.enums.StatusAcaoPDI;
import sdpp_backend.model.enums.StatusPDI;
import sdpp_backend.repository.AcaoPDIRepository;
import sdpp_backend.repository.CicloAvaliacaoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.PDIRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.UsuarioLogado;

/**
 * Plano de Desenvolvimento Individual (RF-PDI-01 a 04).
 * Criado por RH ou gestor (PA-13), sempre por ação humana (RN-04).
 * O colaborador atualiza o progresso das próprias ações (RF-PDI-03).
 */
@Service
public class PDIService {

    private final PDIRepository repository;
    private final AcaoPDIRepository acaoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final CicloAvaliacaoRepository cicloRepository;
    private final AcessoService acesso;

    public PDIService(PDIRepository repository, AcaoPDIRepository acaoRepository,
                      ColaboradorRepository colaboradorRepository, CicloAvaliacaoRepository cicloRepository,
                      AcessoService acesso) {
        this.repository = repository;
        this.acaoRepository = acaoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.cicloRepository = cicloRepository;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<PDIResponse> listar(Long colaboradorId) {
        UsuarioLogado u = acesso.atual();
        List<PDIModel> pdis;
        if (colaboradorId != null) {
            acesso.exigirPodeVer(u, colaborador(colaboradorId));
            pdis = repository.findByColaboradorIdOrderByCriadoEmDesc(colaboradorId);
        } else if (u.isRH()) {
            pdis = repository.findAllByOrderByCriadoEmDesc();
        } else {
            List<Long> ids = acesso.colaboradoresVisiveis(u).stream().map(ColaboradorModel::getId).toList();
            pdis = ids.isEmpty() ? List.of() : repository.findByColaboradorIdInOrderByCriadoEmDesc(ids);
        }
        return pdis.stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public PDIResponse buscarPorId(Long id) {
        PDIModel pdi = buscar(id);
        acesso.exigirPodeVer(acesso.atual(), pdi.getColaborador());
        return paraResponse(pdi);
    }

    @Transactional
    public PDIResponse criar(PDIRequest req) {
        ColaboradorModel c = colaborador(req.colaboradorId());
        acesso.exigirRHOuGestorDe(acesso.atual(), c);
        PDIModel pdi = new PDIModel();
        pdi.setColaborador(c);
        pdi.setGestor(c.getGestor());
        pdi.setCriadoEm(LocalDate.now());
        preencher(pdi, req);
        return paraResponse(repository.save(pdi));
    }

    @Transactional
    public PDIResponse atualizar(Long id, PDIRequest req) {
        PDIModel pdi = buscarEditavel(id);
        if (!pdi.getColaborador().getId().equals(req.colaboradorId())) {
            throw new RegraNegocioException("Não é possível transferir o PDI para outro colaborador.");
        }
        preencher(pdi, req);
        return paraResponse(pdi);
    }

    @Transactional
    public void excluir(Long id) {
        PDIModel pdi = buscarEditavel(id);
        acaoRepository.deleteByPdiId(pdi.getId());
        repository.delete(pdi);
    }

    // ---------- Ações ----------

    @Transactional
    public AcaoResponse adicionarAcao(Long pdiId, AcaoRequest req) {
        PDIModel pdi = buscarEditavel(pdiId);
        AcaoPDIModel acao = new AcaoPDIModel();
        acao.setPdi(pdi);
        preencher(acao, req);
        return paraResponse(acaoRepository.save(acao));
    }

    @Transactional
    public AcaoResponse atualizarAcao(Long acaoId, AcaoRequest req) {
        AcaoPDIModel acao = acao(acaoId);
        buscarEditavel(acao.getPdi().getId());
        preencher(acao, req);
        return paraResponse(acao);
    }

    /** O colaborador só altera as próprias ações (CA-RF-PDI-03); RH e gestor também podem atualizar. */
    @Transactional
    public AcaoResponse atualizarProgresso(Long acaoId, ProgressoRequest req) {
        AcaoPDIModel acao = acao(acaoId);
        UsuarioLogado u = acesso.atual();
        ColaboradorModel dono = acao.getPdi().getColaborador();
        if (!acesso.ehOProprio(u, dono) && !u.isRH() && !acesso.ehGestorDe(u, dono)) {
            throw new AcessoNegadoException("Você só pode atualizar as ações do seu próprio PDI.");
        }
        exigirAtivo(acao.getPdi());
        acao.setStatus(req.status());
        acao.setProgresso(req.status() == StatusAcaoPDI.CONCLUIDO ? 100 : req.progresso());
        return paraResponse(acao);
    }

    @Transactional
    public void excluirAcao(Long acaoId) {
        AcaoPDIModel acao = acao(acaoId);
        buscarEditavel(acao.getPdi().getId());
        acaoRepository.delete(acao);
    }

    // ---------- Apoio ----------

    private void preencher(PDIModel pdi, PDIRequest req) {
        pdi.setCiclo(req.cicloId() == null ? null : cicloRepository.findById(req.cicloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo não encontrado.")));
        pdi.setCargoDesejado(vazioParaNulo(req.cargoDesejado()));
        pdi.setObjetivo(req.objetivo().trim());
        pdi.setHabilidadesDesenvolver(req.habilidadesDesenvolver());
        pdi.setHabilidadesSucesso(req.habilidadesSucesso());
        pdi.setAnotacoes(req.anotacoes());
        if (req.status() != null) {
            pdi.setStatus(req.status());
        }
    }

    /** RN-05: a data de término não pode ser anterior à de início (CA-RF-PDI-02). */
    private void preencher(AcaoPDIModel acao, AcaoRequest req) {
        if (req.dataTermino().isBefore(req.dataInicio())) {
            throw new RegraNegocioException("A data de término da ação não pode ser anterior à data de início.");
        }
        acao.setDescricao(req.descricao().trim());
        acao.setDataInicio(req.dataInicio());
        acao.setDataTermino(req.dataTermino());
        acao.setEntrega(req.entrega());
        acao.setRecursos(req.recursos());
        acao.setStatus(req.status() == null ? StatusAcaoPDI.A_FAZER : req.status());
        int progresso = req.progresso() == null ? 0 : req.progresso();
        acao.setProgresso(acao.getStatus() == StatusAcaoPDI.CONCLUIDO ? 100 : progresso);
    }

    private PDIModel buscarEditavel(Long id) {
        PDIModel pdi = buscar(id);
        acesso.exigirRHOuGestorDe(acesso.atual(), pdi.getColaborador());
        return pdi;
    }

    private void exigirAtivo(PDIModel pdi) {
        if (pdi.getStatus() != StatusPDI.ATIVO) {
            throw new RegraNegocioException("O PDI não está ativo.");
        }
    }

    private PDIModel buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("PDI não encontrado."));
    }

    private AcaoPDIModel acao(Long id) {
        return acaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ação do PDI não encontrada."));
    }

    private ColaboradorModel colaborador(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
    }

    private static String vazioParaNulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public static int progressoGeral(List<AcaoPDIModel> acoes) {
        if (acoes.isEmpty()) {
            return 0;
        }
        return (int) Math.round(acoes.stream().mapToInt(AcaoPDIModel::getProgresso).average().orElse(0));
    }

    public static boolean atrasada(AcaoPDIModel a) {
        return a.getStatus() != StatusAcaoPDI.CONCLUIDO && a.getDataTermino().isBefore(LocalDate.now());
    }

    private AcaoResponse paraResponse(AcaoPDIModel a) {
        return new AcaoResponse(a.getId(), a.getPdi().getId(), a.getDescricao(), a.getDataInicio(),
                a.getDataTermino(), a.getEntrega(), a.getRecursos(), a.getStatus(), a.getProgresso(), atrasada(a));
    }

    private PDIResponse paraResponse(PDIModel p) {
        List<AcaoPDIModel> acoes = p.getId() == null ? List.of() : acaoRepository.findByPdiIdOrderByDataInicio(p.getId());
        ColaboradorModel c = p.getColaborador();
        ColaboradorModel g = p.getGestor();
        return new PDIResponse(p.getId(), c.getId(), c.getNome(), c.getDepartamento().getNome(),
                c.getCargo().getNome(), c.getDataAdmissao(), g == null ? null : g.getId(),
                g == null ? null : g.getNome(), p.getCiclo() == null ? null : p.getCiclo().getId(),
                p.getCiclo() == null ? null : p.getCiclo().getNome(), p.getCargoDesejado(), p.getObjetivo(),
                p.getHabilidadesDesenvolver(), p.getHabilidadesSucesso(), p.getAnotacoes(), p.getStatus(),
                p.getCriadoEm(), progressoGeral(acoes), acoes.stream().map(this::paraResponse).toList());
    }
}
