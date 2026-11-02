package sdpp_backend.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.ColaboradorResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.CicloResumo;
import sdpp_backend.dto.DesenvolvimentoDTOs.HistoricoCicloResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.HistoricoPDIResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.HistoricoResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.IndicadoresResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.PesquisaResumo;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.model.AcaoPDIModel;
import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.NineBoxModel;
import sdpp_backend.model.PDIModel;
import sdpp_backend.model.enums.StatusAcaoPDI;
import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.StatusPDI;
import sdpp_backend.model.enums.StatusPesquisa;
import sdpp_backend.model.enums.TipoAvaliacao;
import sdpp_backend.repository.AcaoPDIRepository;
import sdpp_backend.repository.AvaliacaoRepository;
import sdpp_backend.repository.CicloAvaliacaoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.NineBoxRepository;
import sdpp_backend.repository.PDIRepository;
import sdpp_backend.repository.ParticipacaoClimaRepository;
import sdpp_backend.repository.PesquisaClimaRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.UsuarioLogado;
import sdpp_backend.service.calculo.CalculoMedias;

/**
 * Indicadores e histórico (RF-IND-01, RF-IND-02), sempre restritos ao escopo do usuário:
 * organização (RH), equipe (Gestor) ou o próprio colaborador.
 */
@Service
public class IndicadorService {

    private final AcessoService acesso;
    private final ColaboradorRepository colaboradorRepository;
    private final CicloAvaliacaoRepository cicloRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NineBoxRepository nineBoxRepository;
    private final PDIRepository pdiRepository;
    private final AcaoPDIRepository acaoRepository;
    private final PesquisaClimaRepository pesquisaRepository;
    private final ParticipacaoClimaRepository participacaoRepository;
    private final ResultadoService resultadoService;

    public IndicadorService(AcessoService acesso, ColaboradorRepository colaboradorRepository,
                            CicloAvaliacaoRepository cicloRepository, AvaliacaoRepository avaliacaoRepository,
                            NineBoxRepository nineBoxRepository, PDIRepository pdiRepository,
                            AcaoPDIRepository acaoRepository, PesquisaClimaRepository pesquisaRepository,
                            ParticipacaoClimaRepository participacaoRepository, ResultadoService resultadoService) {
        this.acesso = acesso;
        this.colaboradorRepository = colaboradorRepository;
        this.cicloRepository = cicloRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.nineBoxRepository = nineBoxRepository;
        this.pdiRepository = pdiRepository;
        this.acaoRepository = acaoRepository;
        this.pesquisaRepository = pesquisaRepository;
        this.participacaoRepository = participacaoRepository;
        this.resultadoService = resultadoService;
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse indicadores() {
        UsuarioLogado u = acesso.atual();
        String escopo;
        List<ColaboradorModel> pessoas;
        if (u.isRH()) {
            escopo = "ORGANIZACAO";
            pessoas = colaboradorRepository.findByAtivoTrueOrderByNome();
        } else if (u.isGestor()) {
            escopo = "EQUIPE";
            pessoas = acesso.equipe(u);
        } else {
            escopo = "PESSOAL";
            pessoas = acesso.colaboradoresVisiveis(u);
        }
        Set<Long> ids = pessoas.stream().map(ColaboradorModel::getId).collect(Collectors.toSet());

        CicloAvaliacaoModel ciclo = cicloRepository.findByStatusInOrderByDataInicioDesc(
                List.of(StatusCiclo.LIBERADO, StatusCiclo.RESULTADOS_LIBERADOS)).stream().findFirst().orElse(null);

        long autos = 0;
        long gestores = 0;
        Map<String, Long> distribuicao = new LinkedHashMap<>();
        long pendentesNineBox = 0;
        Double mediaGeral = null;
        if (ciclo != null) {
            List<AvaliacaoModel> avs = avaliacaoRepository.findByCicloId(ciclo.getId()).stream()
                    .filter(a -> ids.contains(a.getColaborador().getId()))
                    .filter(a -> a.getStatus() == StatusAvaliacao.CONCLUIDA).toList();
            autos = avs.stream().filter(a -> a.getTipo() == TipoAvaliacao.AUTOAVALIACAO).count();
            gestores = avs.stream().filter(a -> a.getTipo() == TipoAvaliacao.GESTOR).count();

            if (!escopo.equals("PESSOAL")) {
                List<NineBoxModel> nbs = nineBoxRepository.findByCicloId(ciclo.getId()).stream()
                        .filter(n -> ids.contains(n.getColaborador().getId()) && n.getQuadrante() != null).toList();
                nbs.forEach(n -> distribuicao.merge(n.getQuadrante().getRotulo(), 1L, Long::sum));
                pendentesNineBox = ids.size() - nbs.size();
            }
            if (resultadoService.resultadosDisponiveis(u, ciclo)) {
                var medias = avs.stream().filter(a -> a.getTipo() == TipoAvaliacao.GESTOR)
                        .map(a -> resultadoService.medias(a).geral()).filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue).average();
                mediaGeral = medias.isPresent() ? CalculoMedias.arredondar(medias.getAsDouble()) : null;
            }
        }

        List<PDIModel> pdis = ids.isEmpty() ? List.of()
                : pdiRepository.findByColaboradorIdInOrderByCriadoEmDesc(new ArrayList<>(ids));
        long pdisAtivos = pdis.stream().filter(p -> p.getStatus() == StatusPDI.ATIVO).count();
        List<AcaoPDIModel> acoes = pdis.isEmpty() ? List.of()
                : acaoRepository.findByPdiIdIn(pdis.stream().map(PDIModel::getId).toList());
        Map<String, Long> acoesPorStatus = new LinkedHashMap<>();
        for (StatusAcaoPDI s : StatusAcaoPDI.values()) {
            acoesPorStatus.put(s.name(), acoes.stream().filter(a -> a.getStatus() == s).count());
        }
        long atrasadas = acoes.stream().filter(PDIService::atrasada).count();

        PesquisaResumo pesquisa = null;
        if (u.isRH()) {
            pesquisa = pesquisaRepository.findByStatusOrderByDataInicioDesc(StatusPesquisa.PUBLICADA).stream()
                    .findFirst()
                    .map(p -> new PesquisaResumo(p.getId(), p.getTitulo(), p.getStatus(),
                            participacaoRepository.countByPesquisaId(p.getId()), ids.size()))
                    .orElse(null);
        }

        return new IndicadoresResponse(escopo, ids.size(),
                ciclo == null ? null : new CicloResumo(ciclo.getId(), ciclo.getNome(), ciclo.getStatus()),
                autos, gestores, ciclo == null ? 0 : ids.size(), distribuicao, pendentesNineBox, pdisAtivos,
                acoesPorStatus, atrasadas, pesquisa, mediaGeral);
    }

    /**
     * Histórico do colaborador por ciclo e PDIs. Resultados só após a liberação (exceto RH);
     * a Nine Box não é exibida ao próprio colaborador (matriz de permissões do DRS).
     */
    @Transactional(readOnly = true)
    public HistoricoResponse historico(Long colaboradorId) {
        UsuarioLogado u = acesso.atual();
        ColaboradorModel c = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
        acesso.exigirPodeVer(u, c);
        boolean veNineBox = u.isRH() || acesso.ehGestorDe(u, c);

        List<HistoricoCicloResponse> ciclos = new ArrayList<>();
        for (CicloAvaliacaoModel ciclo : cicloRepository.findByStatusInOrderByDataInicioDesc(
                List.of(StatusCiclo.LIBERADO, StatusCiclo.RESULTADOS_LIBERADOS))) {
            boolean disponivel = resultadoService.resultadosDisponiveis(u, ciclo);
            AvaliacaoModel gestor = resultadoService.avaliacao(c.getId(), ciclo.getId(), TipoAvaliacao.GESTOR);
            AvaliacaoModel auto = resultadoService.avaliacao(c.getId(), ciclo.getId(), TipoAvaliacao.AUTOAVALIACAO);
            NineBoxModel nb = veNineBox && disponivel
                    ? nineBoxRepository.findByColaboradorIdAndCicloId(c.getId(), ciclo.getId()).orElse(null) : null;
            ciclos.add(new HistoricoCicloResponse(ciclo.getId(), ciclo.getNome(), ciclo.getDataInicio(),
                    ciclo.getStatus(), disponivel,
                    disponivel ? resultadoService.medias(gestor) : null,
                    disponivel || Objects.equals(u.colaboradorId(), c.getId()) ? resultadoService.medias(auto) : null,
                    nb == null ? null : nb.getDesempenho(), nb == null ? null : nb.getPotencial(),
                    nb == null || nb.getQuadrante() == null ? null : nb.getQuadrante().getRotulo()));
        }

        List<HistoricoPDIResponse> pdis = pdiRepository.findByColaboradorIdOrderByCriadoEmDesc(c.getId()).stream()
                .map(p -> {
                    var acoes = acaoRepository.findByPdiIdOrderByDataInicio(p.getId());
                    return new HistoricoPDIResponse(p.getId(), p.getObjetivo(), p.getStatus(), p.getCriadoEm(),
                            PDIService.progressoGeral(acoes), acoes.size());
                }).toList();
        return new HistoricoResponse(ColaboradorResponse.de(c), ciclos, pdis);
    }
}
