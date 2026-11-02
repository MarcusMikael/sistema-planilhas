package sdpp_backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.config.SdppProperties;
import sdpp_backend.dto.DesenvolvimentoDTOs.PerguntaRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.PerguntaResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.PesquisaRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.PesquisaResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.RespostaItem;
import sdpp_backend.dto.DesenvolvimentoDTOs.RespostasRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.ResultadoClimaResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.ResultadoPerguntaResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.ResultadoSetorResponse;
import sdpp_backend.exception.ConflitoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.ParticipacaoClimaModel;
import sdpp_backend.model.PerguntaModel;
import sdpp_backend.model.PesquisaClimaModel;
import sdpp_backend.model.RespostaModel;
import sdpp_backend.model.enums.StatusPesquisa;
import sdpp_backend.model.enums.TipoPergunta;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.ParticipacaoClimaRepository;
import sdpp_backend.repository.PerguntaRepository;
import sdpp_backend.repository.PesquisaClimaRepository;
import sdpp_backend.repository.RespostaRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.UsuarioLogado;
import sdpp_backend.service.calculo.CalculoMedias;

/**
 * Pesquisa de clima (RF-CLI-01 a 04, RF-IND-03).
 * Participação (quem respondeu) e conteúdo (o que respondeu) ficam em tabelas separadas (PA-16):
 * assim é possível limitar a uma resposta por campanha (RN-10) sem identificar o conteúdo (RF-CLI-03).
 * Não há rota que exponha respostas individuais; resultados só agregados e com mínimo de respostas (RN-06).
 */
@Service
public class PesquisaClimaService {

    private final PesquisaClimaRepository repository;
    private final PerguntaRepository perguntaRepository;
    private final ParticipacaoClimaRepository participacaoRepository;
    private final RespostaRepository respostaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final AcessoService acesso;
    private final SdppProperties props;

    public PesquisaClimaService(PesquisaClimaRepository repository, PerguntaRepository perguntaRepository,
                                ParticipacaoClimaRepository participacaoRepository,
                                RespostaRepository respostaRepository, ColaboradorRepository colaboradorRepository,
                                AcessoService acesso, SdppProperties props) {
        this.repository = repository;
        this.perguntaRepository = perguntaRepository;
        this.participacaoRepository = participacaoRepository;
        this.respostaRepository = respostaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.acesso = acesso;
        this.props = props;
    }

    /** RH vê todas as campanhas; os demais, só as publicadas. */
    @Transactional(readOnly = true)
    public List<PesquisaResponse> listar() {
        UsuarioLogado u = acesso.atual();
        List<PesquisaClimaModel> lista = u.isRH() ? repository.findAllByOrderByDataInicioDesc()
                : repository.findByStatusOrderByDataInicioDesc(StatusPesquisa.PUBLICADA);
        return lista.stream().map(p -> paraResponse(p, u, false)).toList();
    }

    @Transactional(readOnly = true)
    public PesquisaResponse buscarPorId(Long id) {
        UsuarioLogado u = acesso.atual();
        PesquisaClimaModel p = buscar(id);
        if (!u.isRH() && p.getStatus() != StatusPesquisa.PUBLICADA) {
            throw new RecursoNaoEncontradoException("Pesquisa não encontrada.");
        }
        return paraResponse(p, u, true);
    }

    @Transactional
    public PesquisaResponse criar(PesquisaRequest req) {
        PesquisaClimaModel p = new PesquisaClimaModel();
        preencher(p, req);
        p.setStatus(StatusPesquisa.RASCUNHO);
        p = repository.save(p);
        if (req.usarQuestionarioPadrao() == null || req.usarQuestionarioPadrao()) {
            int ordem = 1;
            for (var q : QuestionarioPadrao.PERGUNTAS) {
                PerguntaModel pergunta = new PerguntaModel();
                pergunta.setPesquisa(p);
                pergunta.setTexto(q.texto());
                pergunta.setTipo(q.tipo());
                pergunta.setObrigatoria(true);
                pergunta.setOrdem(ordem++);
                perguntaRepository.save(pergunta);
            }
        }
        return paraResponse(p, acesso.atual(), true);
    }

    @Transactional
    public PesquisaResponse atualizar(Long id, PesquisaRequest req) {
        PesquisaClimaModel p = buscar(id);
        if (p.getStatus() == StatusPesquisa.ENCERRADA) {
            throw new RegraNegocioException("Pesquisas encerradas não podem ser alteradas.");
        }
        preencher(p, req);
        return paraResponse(p, acesso.atual(), true);
    }

    @Transactional
    public void excluir(Long id) {
        PesquisaClimaModel p = buscarRascunho(id);
        perguntaRepository.deleteAll(perguntaRepository.findByPesquisaIdOrderByOrdem(id));
        repository.delete(p);
    }

    @Transactional
    public PesquisaResponse publicar(Long id) {
        PesquisaClimaModel p = buscarRascunho(id);
        if (perguntaRepository.countByPesquisaId(id) == 0) {
            throw new RegraNegocioException("Adicione ao menos uma pergunta antes de publicar.");
        }
        p.setStatus(StatusPesquisa.PUBLICADA);
        return paraResponse(p, acesso.atual(), true);
    }

    @Transactional
    public PesquisaResponse encerrar(Long id) {
        PesquisaClimaModel p = buscar(id);
        if (p.getStatus() != StatusPesquisa.PUBLICADA) {
            throw new RegraNegocioException("Só pesquisas publicadas podem ser encerradas.");
        }
        p.setStatus(StatusPesquisa.ENCERRADA);
        return paraResponse(p, acesso.atual(), true);
    }

    // ---------- Perguntas (RF-CLI-04: personalização antes de publicar) ----------

    @Transactional
    public PerguntaResponse adicionarPergunta(Long pesquisaId, PerguntaRequest req) {
        PesquisaClimaModel p = buscarRascunho(pesquisaId);
        PerguntaModel q = new PerguntaModel();
        q.setPesquisa(p);
        preencher(q, req, (int) perguntaRepository.countByPesquisaId(pesquisaId) + 1);
        return paraResponse(perguntaRepository.save(q));
    }

    @Transactional
    public PerguntaResponse atualizarPergunta(Long perguntaId, PerguntaRequest req) {
        PerguntaModel q = pergunta(perguntaId);
        buscarRascunho(q.getPesquisa().getId());
        preencher(q, req, q.getOrdem());
        return paraResponse(q);
    }

    @Transactional
    public void excluirPergunta(Long perguntaId) {
        PerguntaModel q = pergunta(perguntaId);
        buscarRascunho(q.getPesquisa().getId());
        perguntaRepository.delete(q);
    }

    // ---------- Resposta (RF-CLI-02, RN-10) ----------

    @Transactional
    public void responder(Long pesquisaId, RespostasRequest req) {
        UsuarioLogado u = acesso.atual();
        Long colaboradorId = acesso.colaboradorIdObrigatorio(u);
        PesquisaClimaModel p = buscar(pesquisaId);
        if (!abertaParaResposta(p)) {
            throw new RegraNegocioException("Esta pesquisa não está aberta para respostas.");
        }
        if (participacaoRepository.existsByPesquisaIdAndColaboradorId(pesquisaId, colaboradorId)) {
            throw new ConflitoException("Você já respondeu esta pesquisa.");
        }
        ColaboradorModel eu = colaboradorRepository.findById(colaboradorId).orElseThrow();
        String setor = eu.getDepartamento().getNome();

        List<PerguntaModel> perguntas = perguntaRepository.findByPesquisaIdOrderByOrdem(pesquisaId);
        Set<Long> vistas = new HashSet<>();
        List<RespostaModel> respostas = new ArrayList<>();
        for (RespostaItem item : req.respostas()) {
            if (!vistas.add(item.perguntaId())) {
                throw new RegraNegocioException("Há respostas repetidas para a mesma pergunta.");
            }
            PerguntaModel q = perguntas.stream().filter(x -> x.getId().equals(item.perguntaId())).findFirst()
                    .orElseThrow(() -> new RegraNegocioException("Pergunta não pertence a esta pesquisa."));
            if (q.getTipo() == TipoPergunta.SETOR) {
                continue;
            }
            RespostaModel r = novaResposta(q, setor);
            if (q.getTipo() == TipoPergunta.ESCALA) {
                if (item.valor() == null) {
                    continue;
                }
                if (item.valor() < 1 || item.valor() > 5) {
                    throw new RegraNegocioException("As respostas de escala devem estar entre 1 e 5.");
                }
                r.setValor(item.valor());
            } else {
                if (item.texto() == null || item.texto().isBlank()) {
                    continue;
                }
                r.setTexto(item.texto().trim());
            }
            respostas.add(r);
        }
        for (PerguntaModel q : perguntas) {
            boolean respondida = q.getTipo() == TipoPergunta.SETOR
                    || respostas.stream().anyMatch(r -> r.getPergunta().getId().equals(q.getId()));
            if (Boolean.TRUE.equals(q.getObrigatoria()) && !respondida) {
                throw new RegraNegocioException("Responda todas as perguntas obrigatórias (falta a nº "
                        + q.getOrdem() + ").");
            }
            if (q.getTipo() == TipoPergunta.SETOR) {
                respostas.add(novaResposta(q, setor));
            }
        }

        ParticipacaoClimaModel participacao = new ParticipacaoClimaModel();
        participacao.setPesquisa(p);
        participacao.setColaborador(eu);
        participacao.setDataParticipacao(LocalDate.now());
        participacaoRepository.save(participacao);
        Collections.shuffle(respostas);
        respostaRepository.saveAll(respostas);
    }

    // ---------- Resultados agregados (RF-IND-03, CA-RF-IND-03) ----------

    @Transactional(readOnly = true)
    public ResultadoClimaResponse resultados(Long pesquisaId) {
        PesquisaClimaModel p = buscar(pesquisaId);
        int minimo = props.clima().minimoRespostas();
        Map<Long, List<RespostaModel>> porPergunta = respostaRepository.findByPerguntaPesquisaId(pesquisaId).stream()
                .collect(Collectors.groupingBy(r -> r.getPergunta().getId()));
        List<ResultadoPerguntaResponse> itens = new ArrayList<>();
        for (PerguntaModel q : perguntaRepository.findByPesquisaIdOrderByOrdem(pesquisaId)) {
            List<RespostaModel> rs = porPergunta.getOrDefault(q.getId(), List.of());
            boolean exibido = rs.size() >= minimo;
            Map<String, List<RespostaModel>> porSetor = new TreeMap<>(rs.stream()
                    .collect(Collectors.groupingBy(r -> Objects.requireNonNullElse(r.getSetor(), "Não informado"))));
            List<ResultadoSetorResponse> setores = porSetor.entrySet().stream().map(e -> {
                boolean ok = e.getValue().size() >= minimo;
                return new ResultadoSetorResponse(e.getKey(), e.getValue().size(), ok,
                        ok && q.getTipo() == TipoPergunta.ESCALA ? media(e.getValue()) : null);
            }).toList();
            Double media = null;
            List<Long> distribuicao = null;
            List<String> textos = null;
            if (exibido && q.getTipo() == TipoPergunta.ESCALA) {
                media = media(rs);
                distribuicao = new ArrayList<>();
                for (int v = 1; v <= 5; v++) {
                    final int valor = v;
                    distribuicao.add(rs.stream().filter(r -> Objects.equals(r.getValor(), valor)).count());
                }
            } else if (exibido && q.getTipo() == TipoPergunta.TEXTO) {
                textos = new ArrayList<>(rs.stream().map(RespostaModel::getTexto).toList());
                Collections.shuffle(textos);
            }
            itens.add(new ResultadoPerguntaResponse(q.getId(), q.getOrdem(), q.getTexto(), q.getTipo(), rs.size(),
                    exibido, media, distribuicao, setores, textos));
        }
        return new ResultadoClimaResponse(p.getId(), p.getTitulo(), p.getStatus(),
                participacaoRepository.countByPesquisaId(pesquisaId), minimo, itens);
    }

    public static Double media(List<RespostaModel> rs) {
        return CalculoMedias.media(rs.stream().map(RespostaModel::getValor).toList());
    }

    // ---------- Apoio ----------

    private static RespostaModel novaResposta(PerguntaModel q, String setor) {
        RespostaModel r = new RespostaModel();
        r.setPergunta(q);
        r.setSetor(setor);
        return r;
    }

    public static boolean abertaParaResposta(PesquisaClimaModel p) {
        LocalDate hoje = LocalDate.now();
        return p.getStatus() == StatusPesquisa.PUBLICADA
                && !hoje.isBefore(p.getDataInicio()) && !hoje.isAfter(p.getDataFim());
    }

    private void preencher(PesquisaClimaModel p, PesquisaRequest req) {
        if (req.dataFim().isBefore(req.dataInicio())) {
            throw new RegraNegocioException("A data de término não pode ser anterior à data de início.");
        }
        p.setTitulo(req.titulo().trim());
        p.setDataInicio(req.dataInicio());
        p.setDataFim(req.dataFim());
    }

    private void preencher(PerguntaModel q, PerguntaRequest req, int ordemPadrao) {
        q.setTexto(req.texto().trim());
        q.setTipo(req.tipo());
        q.setObrigatoria(req.obrigatoria() == null || req.obrigatoria());
        q.setOrdem(req.ordem() == null ? ordemPadrao : req.ordem());
    }

    private PesquisaClimaModel buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pesquisa não encontrada."));
    }

    private PesquisaClimaModel buscarRascunho(Long id) {
        PesquisaClimaModel p = buscar(id);
        if (p.getStatus() != StatusPesquisa.RASCUNHO) {
            throw new RegraNegocioException("As perguntas só podem ser alteradas antes da publicação.");
        }
        return p;
    }

    private PerguntaModel pergunta(Long id) {
        return perguntaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pergunta não encontrada."));
    }

    private PerguntaResponse paraResponse(PerguntaModel q) {
        return new PerguntaResponse(q.getId(), q.getTexto(), q.getTipo(), q.getObrigatoria(), q.getOrdem());
    }

    private PesquisaResponse paraResponse(PesquisaClimaModel p, UsuarioLogado u, boolean comPerguntas) {
        Boolean jaRespondeu = u.colaboradorId() == null ? null
                : participacaoRepository.existsByPesquisaIdAndColaboradorId(p.getId(), u.colaboradorId());
        List<PerguntaResponse> perguntas = comPerguntas
                ? perguntaRepository.findByPesquisaIdOrderByOrdem(p.getId()).stream().map(this::paraResponse).toList()
                : null;
        return new PesquisaResponse(p.getId(), p.getTitulo(), p.getDataInicio(), p.getDataFim(), p.getStatus(),
                perguntaRepository.countByPesquisaId(p.getId()),
                u.isRH() ? participacaoRepository.countByPesquisaId(p.getId()) : 0,
                jaRespondeu, abertaParaResposta(p), perguntas);
    }
}
