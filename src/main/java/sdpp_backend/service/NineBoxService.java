package sdpp_backend.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.config.SdppProperties;
import sdpp_backend.dto.AvaliacaoDTOs.NineBoxItemResponse;
import sdpp_backend.dto.AvaliacaoDTOs.NineBoxRequest;
import sdpp_backend.dto.AvaliacaoDTOs.NineBoxResponse;
import sdpp_backend.dto.AvaliacaoDTOs.QuadranteRequest;
import sdpp_backend.dto.AvaliacaoDTOs.QuadranteResponse;
import sdpp_backend.exception.AcessoNegadoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.NineBoxModel;
import sdpp_backend.model.QuadranteModel;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.NineBoxRepository;
import sdpp_backend.repository.QuadranteRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.UsuarioLogado;
import sdpp_backend.service.calculo.ClassificacaoNineBox;

/**
 * Matriz Nine Box (RF-NBX-01 a 04). Desempenho e potencial são eixos distintos (RN-03);
 * o sistema sugere o desempenho pela média do gestor, mas a decisão é sempre humana (RN-04).
 */
@Service
public class NineBoxService {

    private final NineBoxRepository repository;
    private final QuadranteRepository quadranteRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final CicloService cicloService;
    private final ResultadoService resultadoService;
    private final AcessoService acesso;
    private final SdppProperties props;

    public NineBoxService(NineBoxRepository repository, QuadranteRepository quadranteRepository,
                          ColaboradorRepository colaboradorRepository, CicloService cicloService,
                          ResultadoService resultadoService, AcessoService acesso, SdppProperties props) {
        this.repository = repository;
        this.quadranteRepository = quadranteRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.cicloService = cicloService;
        this.resultadoService = resultadoService;
        this.acesso = acesso;
        this.props = props;
    }

    /** RH vê todos os colaboradores ativos; o gestor vê a sua equipe (RF-NBX-03). */
    @Transactional(readOnly = true)
    public NineBoxResponse matriz(Long cicloId, Long departamentoId) {
        UsuarioLogado u = acesso.atual();
        CicloAvaliacaoModel ciclo = cicloService.buscar(cicloId);
        List<ColaboradorModel> escopo;
        if (u.isRH()) {
            escopo = colaboradorRepository.findByAtivoTrueOrderByNome().stream()
                    .filter(c -> departamentoId == null || c.getDepartamento().getId().equals(departamentoId))
                    .toList();
        } else if (u.isGestor()) {
            escopo = acesso.equipe(u);
        } else {
            throw new AcessoNegadoException("A Nine Box está disponível para RH e gestores.");
        }
        boolean comResultados = resultadoService.resultadosDisponiveis(u, ciclo);
        List<NineBoxItemResponse> itens = escopo.stream().map(c -> item(c, ciclo, comResultados)).toList();
        return new NineBoxResponse(ciclo.getId(), ciclo.getNome(), ciclo.getStatus(), itens, quadrantes());
    }

    /** O gestor registra desempenho e potencial do colaborador da sua equipe (RF-NBX-01). */
    @Transactional
    public NineBoxItemResponse registrar(NineBoxRequest req) {
        UsuarioLogado u = acesso.atual();
        CicloAvaliacaoModel ciclo = cicloService.buscar(req.cicloId());
        if (ciclo.getStatus() != StatusCiclo.RESULTADOS_LIBERADOS) {
            throw new RegraNegocioException("A Nine Box é registrada depois que o RH libera os resultados do ciclo.");
        }
        ColaboradorModel c = colaboradorRepository.findById(req.colaboradorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
        if (!acesso.ehGestorDe(u, c)) {
            throw new AcessoNegadoException("Somente o gestor responsável registra a Nine Box do colaborador.");
        }
        NineBoxModel nb = repository.findByColaboradorIdAndCicloId(c.getId(), ciclo.getId()).orElseGet(() -> {
            NineBoxModel novo = new NineBoxModel();
            novo.setColaborador(c);
            novo.setCiclo(ciclo);
            return novo;
        });
        nb.setDesempenho(req.desempenho());
        nb.setPotencial(req.potencial());
        nb.setObservacoes(req.observacoes());
        nb.setRegistradoPor(colaboradorRepository.findById(u.colaboradorId()).orElseThrow());
        nb.setQuadrante(ClassificacaoNineBox.pendente(req.desempenho(), req.potencial()) ? null
                : quadranteRepository.findByDesempenhoAndPotencial(req.desempenho(), req.potencial()).orElseThrow());
        repository.save(nb);
        return item(c, ciclo, true);
    }

    @Transactional(readOnly = true)
    public List<QuadranteResponse> quadrantes() {
        return quadranteRepository.findAll().stream()
                .sorted(Comparator.comparing((QuadranteModel q) -> q.getPotencial().ordinal()).reversed()
                        .thenComparing(q -> q.getDesempenho().ordinal()))
                .map(NineBoxService::paraResponse).toList();
    }

    /** O RH pode ajustar rótulos e planos de ação sugeridos (PA-08). */
    @Transactional
    public QuadranteResponse atualizarQuadrante(Long id, QuadranteRequest req) {
        QuadranteModel q = quadranteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quadrante não encontrado."));
        q.setRotulo(req.rotulo().trim());
        q.setDescricao(req.descricao().trim());
        q.setPlanoAcao(String.join("\n", req.acoes().stream().map(String::trim).toList()));
        return paraResponse(q);
    }

    public static QuadranteResponse paraResponse(QuadranteModel q) {
        List<String> acoes = Arrays.stream(q.getPlanoAcao().split("\n")).filter(s -> !s.isBlank()).toList();
        return new QuadranteResponse(q.getId(), q.getDesempenho(), q.getPotencial(), q.getRotulo(),
                q.getDescricao(), acoes);
    }

    private NineBoxItemResponse item(ColaboradorModel c, CicloAvaliacaoModel ciclo, boolean comResultados) {
        NineBoxModel nb = repository.findByColaboradorIdAndCicloId(c.getId(), ciclo.getId()).orElse(null);
        Double media = comResultados ? resultadoService.mediaGeralGestor(c.getId(), ciclo.getId()) : null;
        var sugestao = ClassificacaoNineBox.sugerirDesempenho(media, props.ninebox().limiteMedio(),
                props.ninebox().limiteAlto());
        boolean pendente = nb == null || ClassificacaoNineBox.pendente(nb.getDesempenho(), nb.getPotencial());
        return new NineBoxItemResponse(c.getId(), c.getNome(), c.getCargo().getNome(), c.getDepartamento().getNome(),
                c.getGestor() == null ? null : c.getGestor().getId(), nb == null ? null : nb.getId(), nb == null ? null : nb.getDesempenho(),
                nb == null ? null : nb.getPotencial(), media, sugestao, pendente,
                nb == null || nb.getQuadrante() == null ? null : paraResponse(nb.getQuadrante()),
                nb == null ? null : nb.getObservacoes());
    }
}
