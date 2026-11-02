package sdpp_backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.AvaliacaoDTOs.AbrirAvaliacaoRequest;
import sdpp_backend.dto.AvaliacaoDTOs.AvaliacaoResponse;
import sdpp_backend.dto.AvaliacaoDTOs.ItemAvaliacaoResponse;
import sdpp_backend.dto.AvaliacaoDTOs.NotaItem;
import sdpp_backend.dto.AvaliacaoDTOs.NotasRequest;
import sdpp_backend.dto.AvaliacaoDTOs.PendenciaResponse;
import sdpp_backend.exception.AcessoNegadoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.model.CicloCompetenciaModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.PontuacaoModel;
import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.TipoAvaliacao;
import sdpp_backend.repository.AvaliacaoRepository;
import sdpp_backend.repository.CicloAvaliacaoRepository;
import sdpp_backend.repository.CicloCompetenciaRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.PontuacaoRepository;
import sdpp_backend.security.AcessoService;
import sdpp_backend.security.UsuarioLogado;

/**
 * Autoavaliação e avaliação do gestor (RF-AVA-01 a 03).
 * Os dois tipos usam as mesmas competências, mas são registros independentes (RN-01).
 */
@Service
public class AvaliacaoService {

    private final AvaliacaoRepository repository;
    private final PontuacaoRepository pontuacaoRepository;
    private final CicloAvaliacaoRepository cicloRepository;
    private final CicloCompetenciaRepository cicloCompetenciaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final AcessoService acesso;

    public AvaliacaoService(AvaliacaoRepository repository, PontuacaoRepository pontuacaoRepository,
                            CicloAvaliacaoRepository cicloRepository,
                            CicloCompetenciaRepository cicloCompetenciaRepository,
                            ColaboradorRepository colaboradorRepository, AcessoService acesso) {
        this.repository = repository;
        this.pontuacaoRepository = pontuacaoRepository;
        this.cicloRepository = cicloRepository;
        this.cicloCompetenciaRepository = cicloCompetenciaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.acesso = acesso;
    }

    /** Avaliações que o usuário atual deve fazer nos ciclos liberados. */
    @Transactional(readOnly = true)
    public List<PendenciaResponse> pendentes() {
        UsuarioLogado u = acesso.atual();
        List<PendenciaResponse> lista = new ArrayList<>();
        if (u.colaboradorId() == null) {
            return lista;
        }
        ColaboradorModel eu = colaboradorRepository.findById(u.colaboradorId()).orElseThrow();
        for (CicloAvaliacaoModel ciclo : cicloRepository.findByStatusInOrderByDataInicioDesc(
                List.of(StatusCiclo.LIBERADO))) {
            lista.add(pendencia(ciclo, eu, TipoAvaliacao.AUTOAVALIACAO));
            if (u.podeGerirEquipe()) {
                acesso.equipe(u).forEach(membro -> lista.add(pendencia(ciclo, membro, TipoAvaliacao.GESTOR)));
            }
        }
        return lista;
    }

    /** Avaliações visíveis ao usuário em um ciclo (sem as notas). */
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listar(Long cicloId) {
        UsuarioLogado u = acesso.atual();
        return repository.findByCicloId(cicloId).stream()
                .filter(a -> podeVer(u, a))
                .map(a -> paraResponse(a, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPorId(Long id) {
        AvaliacaoModel a = buscar(id);
        if (!podeVer(acesso.atual(), a)) {
            throw new AcessoNegadoException("Os resultados desta avaliação ainda não estão disponíveis para você.");
        }
        return paraResponse(a, true);
    }

    /**
     * Abre (ou retorna, se já existir) a avaliação do tipo informado.
     * Autoavaliação: só o próprio colaborador. Avaliação do gestor: só o gestor da equipe.
     */
    @Transactional
    public AvaliacaoResponse abrir(AbrirAvaliacaoRequest req) {
        UsuarioLogado u = acesso.atual();
        Long meuId = acesso.colaboradorIdObrigatorio(u);
        CicloAvaliacaoModel ciclo = cicloRepository.findById(req.cicloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo não encontrado."));
        exigirCicloAberto(ciclo);
        ColaboradorModel avaliado = colaboradorRepository.findById(req.colaboradorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
        if (!Boolean.TRUE.equals(avaliado.getAtivo())) {
            throw new RegraNegocioException("O colaborador está inativo.");
        }
        if (req.tipo() == TipoAvaliacao.AUTOAVALIACAO && !avaliado.getId().equals(meuId)) {
            throw new AcessoNegadoException("A autoavaliação só pode ser feita pelo próprio colaborador.");
        }
        if (req.tipo() == TipoAvaliacao.GESTOR && !acesso.ehGestorDe(u, avaliado)) {
            throw new AcessoNegadoException("Você só pode avaliar colaboradores da sua equipe.");
        }
        var existente = repository.findByColaboradorIdAndCicloIdAndTipo(avaliado.getId(), ciclo.getId(), req.tipo());
        if (existente.isPresent()) {
            return paraResponse(existente.get(), true);
        }
        AvaliacaoModel a = new AvaliacaoModel();
        a.setCiclo(ciclo);
        a.setColaborador(avaliado);
        a.setAvaliador(colaboradorRepository.findById(meuId).orElseThrow());
        a.setTipo(req.tipo());
        a.setStatus(StatusAvaliacao.EM_ANDAMENTO);
        a = repository.save(a);
        for (CicloCompetenciaModel cc : competenciasDoCiclo(ciclo.getId())) {
            PontuacaoModel p = new PontuacaoModel();
            p.setAvaliacao(a);
            p.setCicloCompetencia(cc);
            pontuacaoRepository.save(p);
        }
        return paraResponse(a, true);
    }

    /**
     * Salva o rascunho das notas. Notas fora de 0 a 5 são rejeitadas (RN-07) e, por ser
     * transacional, nenhuma nota é gravada parcialmente (RNF-CON-02).
     */
    @Transactional
    public AvaliacaoResponse salvarNotas(Long id, NotasRequest req) {
        AvaliacaoModel a = buscarEditavel(id);
        List<PontuacaoModel> pontuacoes = pontuacoes(a.getId());
        for (NotaItem item : req.notas()) {
            if (item.nota() != null && (item.nota() < 0 || item.nota() > 5)) {
                throw new RegraNegocioException("A nota deve estar entre 0 e 5.");
            }
            PontuacaoModel p = pontuacoes.stream()
                    .filter(x -> x.getCicloCompetencia().getId().equals(item.cicloCompetenciaId()))
                    .findFirst()
                    .orElseThrow(() -> new RegraNegocioException("Competência não pertence a este ciclo."));
            p.setNota(item.nota());
            p.setComentario(item.comentario());
        }
        return paraResponse(a, true);
    }

    /** Conclui a avaliação; exige todas as notas preenchidas (RN-02) e registra a data. */
    @Transactional
    public AvaliacaoResponse concluir(Long id) {
        AvaliacaoModel a = buscarEditavel(id);
        List<PontuacaoModel> pontuacoes = pontuacoes(a.getId());
        long faltam = pontuacoes.stream().filter(p -> p.getNota() == null).count();
        if (pontuacoes.isEmpty() || faltam > 0) {
            throw new RegraNegocioException("Preencha todas as notas antes de concluir (faltam " + faltam + ").");
        }
        a.setStatus(StatusAvaliacao.CONCLUIDA);
        a.setDataAvaliacao(LocalDate.now());
        return paraResponse(a, true);
    }

    // ---------- Regras de visibilidade ----------

    /**
     * Quem avaliou sempre vê a própria avaliação. RH vê todas (analisa antes de liberar).
     * Gestor e colaborador veem os demais registros só depois da liberação (RN-09).
     */
    private boolean podeVer(UsuarioLogado u, AvaliacaoModel a) {
        if (u.isRH() || Objects.equals(u.colaboradorId(), a.getAvaliador().getId())) {
            return true;
        }
        return a.getCiclo().getStatus() == StatusCiclo.RESULTADOS_LIBERADOS
                && a.getStatus() == StatusAvaliacao.CONCLUIDA
                && acesso.podeVer(u, a.getColaborador());
    }

    private AvaliacaoModel buscarEditavel(Long id) {
        AvaliacaoModel a = buscar(id);
        UsuarioLogado u = acesso.atual();
        if (!Objects.equals(u.colaboradorId(), a.getAvaliador().getId())) {
            throw new AcessoNegadoException("Somente o avaliador pode alterar esta avaliação.");
        }
        if (a.getStatus() == StatusAvaliacao.CONCLUIDA) {
            throw new RegraNegocioException("A avaliação já foi concluída.");
        }
        exigirCicloAberto(a.getCiclo());
        return a;
    }

    private void exigirCicloAberto(CicloAvaliacaoModel ciclo) {
        if (ciclo.getStatus() != StatusCiclo.LIBERADO) {
            throw new RegraNegocioException("O ciclo não está aberto para avaliações.");
        }
    }

    // ---------- Montagem das respostas ----------

    private PendenciaResponse pendencia(CicloAvaliacaoModel ciclo, ColaboradorModel c, TipoAvaliacao tipo) {
        var a = repository.findByColaboradorIdAndCicloIdAndTipo(c.getId(), ciclo.getId(), tipo).orElse(null);
        return new PendenciaResponse(ciclo.getId(), ciclo.getNome(), ciclo.getDataFim(), c.getId(), c.getNome(),
                c.getCargo().getNome(), tipo, a == null ? null : a.getId(), a == null ? null : a.getStatus());
    }

    private List<CicloCompetenciaModel> competenciasDoCiclo(Long cicloId) {
        return cicloCompetenciaRepository.findByCicloIdOrderByCompetenciaTipoAscCompetenciaNomeAsc(cicloId);
    }

    private List<PontuacaoModel> pontuacoes(Long avaliacaoId) {
        return pontuacaoRepository
                .findByAvaliacaoIdOrderByCicloCompetenciaCompetenciaTipoAscCicloCompetenciaCompetenciaNomeAsc(avaliacaoId);
    }

    private AvaliacaoModel buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));
    }

    private AvaliacaoResponse paraResponse(AvaliacaoModel a, boolean comItens) {
        List<PontuacaoModel> ps = pontuacoes(a.getId());
        int preenchidas = (int) ps.stream().filter(p -> p.getNota() != null).count();
        List<ItemAvaliacaoResponse> itens = !comItens ? null : ps.stream().map(p -> {
            var comp = p.getCicloCompetencia().getCompetencia();
            return new ItemAvaliacaoResponse(p.getCicloCompetencia().getId(), comp.getId(), comp.getNome(),
                    comp.getDescricao(), comp.getTipo(), p.getNota(), p.getComentario());
        }).toList();
        ColaboradorModel c = a.getColaborador();
        ColaboradorModel av = a.getAvaliador();
        return new AvaliacaoResponse(a.getId(), a.getCiclo().getId(), a.getCiclo().getNome(), a.getCiclo().getStatus(),
                c.getId(), c.getNome(), c.getCargo().getNome(), av.getId(), av.getNome(), av.getCargo().getNome(),
                a.getTipo(), a.getStatus(), a.getDataAvaliacao(), preenchidas, ps.size(), itens);
    }
}
