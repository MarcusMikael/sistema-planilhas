package sdpp_backend.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.AvaliacaoDTOs.CicloRequest;
import sdpp_backend.dto.AvaliacaoDTOs.CicloResponse;
import sdpp_backend.dto.AvaliacaoDTOs.CompetenciaCicloResponse;
import sdpp_backend.dto.AvaliacaoDTOs.SelecaoCompetenciasRequest;
import sdpp_backend.dto.CadastroDTOs.CompetenciaResponse;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.model.CicloCompetenciaModel;
import sdpp_backend.model.CompetenciaModel;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.TipoCompetencia;
import sdpp_backend.repository.AvaliacaoRepository;
import sdpp_backend.repository.CicloAvaliacaoRepository;
import sdpp_backend.repository.CicloCompetenciaRepository;
import sdpp_backend.repository.CompetenciaRepository;
import sdpp_backend.security.AcessoService;

/**
 * Ciclos de avaliação (RF-CIC-01 a 03). Fluxo: RASCUNHO → LIBERADO → RESULTADOS_LIBERADOS.
 * Cada ciclo usa exatamente 5 competências comportamentais e 5 técnicas (RN-08).
 */
@Service
public class CicloService {

    public static final int COMPETENCIAS_POR_TIPO = 5;

    private final CicloAvaliacaoRepository repository;
    private final CicloCompetenciaRepository cicloCompetenciaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AcessoService acesso;

    public CicloService(CicloAvaliacaoRepository repository, CicloCompetenciaRepository cicloCompetenciaRepository,
                        CompetenciaRepository competenciaRepository, AvaliacaoRepository avaliacaoRepository,
                        AcessoService acesso) {
        this.repository = repository;
        this.cicloCompetenciaRepository = cicloCompetenciaRepository;
        this.competenciaRepository = competenciaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.acesso = acesso;
    }

    /** RH vê todos; os demais perfis só veem ciclos já liberados. */
    @Transactional(readOnly = true)
    public List<CicloResponse> listar() {
        List<CicloAvaliacaoModel> ciclos = acesso.atual().isRH()
                ? repository.findAllByOrderByDataInicioDesc()
                : repository.findByStatusInOrderByDataInicioDesc(
                        List.of(StatusCiclo.LIBERADO, StatusCiclo.RESULTADOS_LIBERADOS));
        return ciclos.stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public CicloResponse buscarPorId(Long id) {
        CicloAvaliacaoModel ciclo = buscar(id);
        if (!acesso.atual().isRH() && ciclo.getStatus() == StatusCiclo.RASCUNHO) {
            throw new RecursoNaoEncontradoException("Ciclo não encontrado.");
        }
        return paraResponse(ciclo);
    }

    @Transactional
    public CicloResponse salvar(CicloRequest req) {
        CicloAvaliacaoModel ciclo = new CicloAvaliacaoModel();
        preencher(ciclo, req);
        return paraResponse(repository.save(ciclo));
    }

    @Transactional
    public CicloResponse atualizar(Long id, CicloRequest req) {
        CicloAvaliacaoModel ciclo = buscar(id);
        if (ciclo.getStatus() == StatusCiclo.RESULTADOS_LIBERADOS) {
            throw new RegraNegocioException("Ciclos com resultados liberados não podem ser alterados.");
        }
        preencher(ciclo, req);
        return paraResponse(ciclo);
    }

    /** O RH marca "Sim" nas competências do catálogo; no máximo 5 de cada tipo (CA-RF-CIC-02). */
    @Transactional
    public CicloResponse selecionarCompetencias(Long id, SelecaoCompetenciasRequest req) {
        CicloAvaliacaoModel ciclo = buscar(id);
        if (ciclo.getStatus() != StatusCiclo.RASCUNHO) {
            throw new RegraNegocioException("As competências só podem ser alteradas antes de liberar o ciclo.");
        }
        List<Long> ids = req.competenciaIds();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new RegraNegocioException("Há competências repetidas na seleção.");
        }
        List<CompetenciaModel> selecionadas = competenciaRepository.findAllById(ids);
        if (selecionadas.size() != ids.size()) {
            throw new RecursoNaoEncontradoException("Uma ou mais competências não foram encontradas.");
        }
        if (selecionadas.stream().anyMatch(c -> !Boolean.TRUE.equals(c.getAtivo()))) {
            throw new RegraNegocioException("Competências inativas não podem ser selecionadas.");
        }
        for (TipoCompetencia tipo : TipoCompetencia.values()) {
            long qtd = selecionadas.stream().filter(c -> c.getTipo() == tipo).count();
            if (qtd > COMPETENCIAS_POR_TIPO) {
                throw new RegraNegocioException("Selecione no máximo " + COMPETENCIAS_POR_TIPO
                        + " competências do tipo " + rotulo(tipo) + " (foram " + qtd + ").");
            }
        }
        cicloCompetenciaRepository.deleteByCicloId(id);
        cicloCompetenciaRepository.flush();
        for (CompetenciaModel c : selecionadas) {
            CicloCompetenciaModel cc = new CicloCompetenciaModel();
            cc.setCiclo(ciclo);
            cc.setCompetencia(c);
            cicloCompetenciaRepository.save(cc);
        }
        return paraResponse(ciclo);
    }

    /** Libera o ciclo para as avaliações (RF-CIC-03); exige 5 + 5 competências. */
    @Transactional
    public CicloResponse liberar(Long id) {
        CicloAvaliacaoModel ciclo = buscar(id);
        if (ciclo.getStatus() != StatusCiclo.RASCUNHO) {
            throw new RegraNegocioException("O ciclo já foi liberado.");
        }
        CicloResponse atual = paraResponse(ciclo);
        if (atual.qtdComportamentais() != COMPETENCIAS_POR_TIPO || atual.qtdTecnicas() != COMPETENCIAS_POR_TIPO) {
            throw new RegraNegocioException("O ciclo precisa de exatamente 5 competências comportamentais e 5 técnicas"
                    + " (atual: " + atual.qtdComportamentais() + " e " + atual.qtdTecnicas() + ").");
        }
        ciclo.setStatus(StatusCiclo.LIBERADO);
        return paraResponse(ciclo);
    }

    /** Torna os resultados consultáveis por gestores e colaboradores (RF-AVA-05, RN-09). */
    @Transactional
    public CicloResponse liberarResultados(Long id) {
        CicloAvaliacaoModel ciclo = buscar(id);
        if (ciclo.getStatus() != StatusCiclo.LIBERADO) {
            throw new RegraNegocioException("Só é possível liberar resultados de um ciclo em andamento.");
        }
        ciclo.setStatus(StatusCiclo.RESULTADOS_LIBERADOS);
        return paraResponse(ciclo);
    }

    @Transactional
    public void excluir(Long id) {
        CicloAvaliacaoModel ciclo = buscar(id);
        if (ciclo.getStatus() != StatusCiclo.RASCUNHO || avaliacaoRepository.existsByCicloId(id)) {
            throw new RegraNegocioException("Só ciclos em rascunho, sem avaliações, podem ser excluídos.");
        }
        cicloCompetenciaRepository.deleteByCicloId(id);
        repository.delete(ciclo);
    }

    public CicloAvaliacaoModel buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo não encontrado."));
    }

    private void preencher(CicloAvaliacaoModel ciclo, CicloRequest req) {
        if (req.dataFim().isBefore(req.dataInicio())) {
            throw new RegraNegocioException("A data de término não pode ser anterior à data de início.");
        }
        ciclo.setNome(req.nome().trim());
        ciclo.setDataInicio(req.dataInicio());
        ciclo.setDataFim(req.dataFim());
    }

    private CicloResponse paraResponse(CicloAvaliacaoModel ciclo) {
        List<CicloCompetenciaModel> ccs = ciclo.getId() == null ? List.of()
                : cicloCompetenciaRepository.findByCicloIdOrderByCompetenciaTipoAscCompetenciaNomeAsc(ciclo.getId());
        long comp = ccs.stream().filter(cc -> cc.getCompetencia().getTipo() == TipoCompetencia.COMPORTAMENTAL).count();
        long tec = ccs.size() - comp;
        var competencias = ccs.stream()
                .map(cc -> new CompetenciaCicloResponse(cc.getId(), CompetenciaResponse.de(cc.getCompetencia())))
                .toList();
        return new CicloResponse(ciclo.getId(), ciclo.getNome(), ciclo.getDataInicio(), ciclo.getDataFim(),
                ciclo.getStatus(), comp, tec, competencias);
    }

    private static String rotulo(TipoCompetencia tipo) {
        return tipo == TipoCompetencia.COMPORTAMENTAL ? "comportamental" : "técnica";
    }
}
