package sdpp_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.AvaliacaoDTOs.ColaboradorRef;
import sdpp_backend.dto.AvaliacaoDTOs.ItemComparacaoResponse;
import sdpp_backend.dto.AvaliacaoDTOs.LinhaMapaResponse;
import sdpp_backend.dto.AvaliacaoDTOs.MapaCompetenciasResponse;
import sdpp_backend.dto.AvaliacaoDTOs.MediasResponse;
import sdpp_backend.dto.AvaliacaoDTOs.ResultadoColaboradorResponse;
import sdpp_backend.exception.AcessoNegadoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.model.CicloCompetenciaModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.PontuacaoModel;
import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.TipoAvaliacao;
import sdpp_backend.model.enums.TipoCompetencia;
import sdpp_backend.repository.AvaliacaoRepository;
import sdpp_backend.repository.CicloAvaliacaoRepository;
import sdpp_backend.repository.CicloCompetenciaRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.PontuacaoRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.UsuarioLogado;
import sdpp_backend.service.calculo.CalculoMedias;

/**
 * Cálculo e consulta de resultados (RF-AVA-04, 05, 06, 08; RF-IND-04; RN-09, RN-11).
 * Só entram nas médias avaliações concluídas; notas ausentes são ignoradas (RN-02).
 * O resultado oficial é o do gestor; a autoavaliação é exibida para comparação (PA-01).
 */
@Service
public class ResultadoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PontuacaoRepository pontuacaoRepository;
    private final CicloAvaliacaoRepository cicloRepository;
    private final CicloCompetenciaRepository cicloCompetenciaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final AcessoService acesso;

    public ResultadoService(AvaliacaoRepository avaliacaoRepository, PontuacaoRepository pontuacaoRepository,
                            CicloAvaliacaoRepository cicloRepository,
                            CicloCompetenciaRepository cicloCompetenciaRepository,
                            ColaboradorRepository colaboradorRepository, AcessoService acesso) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pontuacaoRepository = pontuacaoRepository;
        this.cicloRepository = cicloRepository;
        this.cicloCompetenciaRepository = cicloCompetenciaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.acesso = acesso;
    }

    /** RH consulta a qualquer momento; gestor e colaborador só após a liberação (CA-RF-AVA-05/06). */
    @Transactional(readOnly = true)
    public ResultadoColaboradorResponse resultadoColaborador(Long cicloId, Long colaboradorId) {
        UsuarioLogado u = acesso.atual();
        CicloAvaliacaoModel ciclo = ciclo(cicloId);
        ColaboradorModel c = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
        acesso.exigirPodeVer(u, c);
        exigirResultadosDisponiveis(u, ciclo);

        AvaliacaoModel gestor = avaliacao(c.getId(), cicloId, TipoAvaliacao.GESTOR);
        AvaliacaoModel auto = avaliacao(c.getId(), cicloId, TipoAvaliacao.AUTOAVALIACAO);
        List<PontuacaoModel> pg = notasConcluidas(gestor);
        List<PontuacaoModel> pa = notasConcluidas(auto);

        List<ItemComparacaoResponse> itens = new ArrayList<>();
        for (CicloCompetenciaModel cc : competencias(cicloId)) {
            itens.add(new ItemComparacaoResponse(cc.getCompetencia().getNome(), cc.getCompetencia().getTipo(),
                    notaDe(pg, cc), notaDe(pa, cc)));
        }
        return new ResultadoColaboradorResponse(ciclo.getId(), ciclo.getNome(), ciclo.getStatus(), c.getId(),
                c.getNome(), c.getCargo().getNome(), c.getDepartamento().getNome(),
                medias(gestor), medias(auto), itens);
    }

    /** Mapa de competências: competência × colaborador, com a média de cada competência (RF-IND-04). */
    @Transactional(readOnly = true)
    public MapaCompetenciasResponse mapa(Long cicloId) {
        UsuarioLogado u = acesso.atual();
        if (!u.isRH() && !u.isGestor()) {
            throw new AcessoNegadoException("O mapa de competências está disponível para RH e gestores.");
        }
        CicloAvaliacaoModel ciclo = ciclo(cicloId);
        exigirResultadosDisponiveis(u, ciclo);

        List<AvaliacaoModel> concluidas = avaliacaoRepository.findByCicloId(cicloId).stream()
                .filter(a -> a.getStatus() == StatusAvaliacao.CONCLUIDA)
                .filter(a -> u.isRH() || acesso.ehGestorDe(u, a.getColaborador()))
                .toList();
        List<AvaliacaoModel> doGestor = concluidas.stream().filter(a -> a.getTipo() == TipoAvaliacao.GESTOR)
                .sorted((a, b) -> a.getColaborador().getNome().compareToIgnoreCase(b.getColaborador().getNome()))
                .toList();
        List<AvaliacaoModel> autos = concluidas.stream().filter(a -> a.getTipo() == TipoAvaliacao.AUTOAVALIACAO)
                .toList();

        List<Long> ids = concluidas.stream().map(AvaliacaoModel::getId).toList();
        Map<Long, List<PontuacaoModel>> porAvaliacao = ids.isEmpty() ? Map.of()
                : pontuacaoRepository.findByAvaliacaoIdIn(ids).stream()
                .collect(Collectors.groupingBy(p -> p.getAvaliacao().getId()));

        List<ColaboradorRef> colunas = doGestor.stream()
                .map(a -> new ColaboradorRef(a.getColaborador().getId(), a.getColaborador().getNome())).toList();
        List<LinhaMapaResponse> comportamentais = new ArrayList<>();
        List<LinhaMapaResponse> tecnicas = new ArrayList<>();
        for (CicloCompetenciaModel cc : competencias(cicloId)) {
            List<Integer> notasGestor = doGestor.stream()
                    .map(a -> notaDe(porAvaliacao.getOrDefault(a.getId(), List.of()), cc)).toList();
            List<Integer> notasAuto = autos.stream()
                    .map(a -> notaDe(porAvaliacao.getOrDefault(a.getId(), List.of()), cc)).toList();
            var linha = new LinhaMapaResponse(cc.getCompetencia().getId(), cc.getCompetencia().getNome(),
                    notasGestor, CalculoMedias.media(notasGestor), CalculoMedias.media(notasAuto));
            (cc.getCompetencia().getTipo() == TipoCompetencia.COMPORTAMENTAL ? comportamentais : tecnicas).add(linha);
        }
        return new MapaCompetenciasResponse(ciclo.getId(), ciclo.getNome(), colunas, comportamentais, tecnicas);
    }

    // ---------- Apoio a outros módulos ----------

    /** Médias de uma avaliação (comportamental, técnica e geral); nulas se não concluída. */
    public MediasResponse medias(AvaliacaoModel a) {
        if (a == null) {
            return null;
        }
        if (a.getStatus() != StatusAvaliacao.CONCLUIDA) {
            return new MediasResponse(a.getStatus(), null, null, null);
        }
        List<PontuacaoModel> ps = notasConcluidas(a);
        Function<TipoCompetencia, Double> porTipo = tipo -> CalculoMedias.media(ps.stream()
                .filter(p -> p.getCicloCompetencia().getCompetencia().getTipo() == tipo)
                .map(PontuacaoModel::getNota).toList());
        return new MediasResponse(a.getStatus(), porTipo.apply(TipoCompetencia.COMPORTAMENTAL),
                porTipo.apply(TipoCompetencia.TECNICA),
                CalculoMedias.media(ps.stream().map(PontuacaoModel::getNota).toList()));
    }

    /** Média geral da avaliação do gestor concluída, ou null. */
    public Double mediaGeralGestor(Long colaboradorId, Long cicloId) {
        MediasResponse m = medias(avaliacao(colaboradorId, cicloId, TipoAvaliacao.GESTOR));
        return m == null ? null : m.geral();
    }

    public AvaliacaoModel avaliacao(Long colaboradorId, Long cicloId, TipoAvaliacao tipo) {
        return avaliacaoRepository.findByColaboradorIdAndCicloIdAndTipo(colaboradorId, cicloId, tipo).orElse(null);
    }

    public boolean resultadosDisponiveis(UsuarioLogado u, CicloAvaliacaoModel ciclo) {
        return ciclo.getStatus() == StatusCiclo.RESULTADOS_LIBERADOS
                || (u.isRH() && ciclo.getStatus() == StatusCiclo.LIBERADO);
    }

    private void exigirResultadosDisponiveis(UsuarioLogado u, CicloAvaliacaoModel ciclo) {
        if (!resultadosDisponiveis(u, ciclo)) {
            throw new AcessoNegadoException("Os resultados deste ciclo ainda não foram liberados pelo RH.");
        }
    }

    private List<PontuacaoModel> notasConcluidas(AvaliacaoModel a) {
        if (a == null || a.getStatus() != StatusAvaliacao.CONCLUIDA) {
            return List.of();
        }
        return pontuacaoRepository
                .findByAvaliacaoIdOrderByCicloCompetenciaCompetenciaTipoAscCicloCompetenciaCompetenciaNomeAsc(a.getId());
    }

    private static Integer notaDe(List<PontuacaoModel> ps, CicloCompetenciaModel cc) {
        return ps.stream().filter(p -> Objects.equals(p.getCicloCompetencia().getId(), cc.getId()))
                .map(PontuacaoModel::getNota).findFirst().orElse(null);
    }

    private List<CicloCompetenciaModel> competencias(Long cicloId) {
        return cicloCompetenciaRepository.findByCicloIdOrderByCompetenciaTipoAscCompetenciaNomeAsc(cicloId);
    }

    private CicloAvaliacaoModel ciclo(Long id) {
        return cicloRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo não encontrado."));
    }
}
