package sdpp_backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sdpp_backend.dto.AvaliacaoDTOs.MediasResponse;
import sdpp_backend.dto.CadastroDTOs.ColaboradorResponse;
import sdpp_backend.model.enums.Nivel;
import sdpp_backend.model.enums.StatusAcaoPDI;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.StatusPDI;
import sdpp_backend.model.enums.StatusPesquisa;
import sdpp_backend.model.enums.TipoPergunta;

/** DTOs dos módulos PDI, CLI e IND. */
public final class DesenvolvimentoDTOs {

    private DesenvolvimentoDTOs() {
    }

    // ---------- PDI ----------

    public record PDIRequest(
            @NotNull(message = "Informe o colaborador") Long colaboradorId,
            Long cicloId,
            @Size(max = 150) String cargoDesejado,
            @NotBlank(message = "Informe o objetivo principal") @Size(max = 2000) String objetivo,
            @Size(max = 2000) String habilidadesDesenvolver,
            @Size(max = 2000) String habilidadesSucesso,
            @Size(max = 4000) String anotacoes,
            StatusPDI status) {
    }

    public record AcaoRequest(
            @NotBlank(message = "Descreva a ação") @Size(max = 1000) String descricao,
            @NotNull(message = "Informe a data de início") LocalDate dataInicio,
            @NotNull(message = "Informe a data de término") LocalDate dataTermino,
            @Size(max = 1000) String entrega,
            @Size(max = 500) String recursos,
            StatusAcaoPDI status,
            @Min(value = 0, message = "O progresso vai de 0 a 100")
            @Max(value = 100, message = "O progresso vai de 0 a 100") Integer progresso) {
    }

    public record ProgressoRequest(
            @NotNull(message = "Informe o status") StatusAcaoPDI status,
            @NotNull(message = "Informe o progresso")
            @Min(value = 0, message = "O progresso vai de 0 a 100")
            @Max(value = 100, message = "O progresso vai de 0 a 100") Integer progresso) {
    }

    public record AcaoResponse(Long id, Long pdiId, String descricao, LocalDate dataInicio, LocalDate dataTermino,
                               String entrega, String recursos, StatusAcaoPDI status, Integer progresso,
                               boolean atrasada) {
    }

    public record PDIResponse(Long id, Long colaboradorId, String colaboradorNome, String departamento,
                              String cargoAtual, LocalDate dataAdmissao, Long gestorId, String gestorNome,
                              Long cicloId, String cicloNome, String cargoDesejado, String objetivo,
                              String habilidadesDesenvolver, String habilidadesSucesso, String anotacoes,
                              StatusPDI status, LocalDate criadoEm, int progressoGeral, List<AcaoResponse> acoes) {
    }

    // ---------- Pesquisa de clima ----------

    public record PesquisaRequest(
            @NotBlank(message = "Informe o título") @Size(max = 200) String titulo,
            @NotNull(message = "Informe a data de início") LocalDate dataInicio,
            @NotNull(message = "Informe a data de término") LocalDate dataFim,
            Boolean usarQuestionarioPadrao) {
    }

    public record PerguntaRequest(
            @NotBlank(message = "Informe o texto da pergunta") @Size(max = 500) String texto,
            @NotNull(message = "Informe o tipo") TipoPergunta tipo,
            Boolean obrigatoria,
            Integer ordem) {
    }

    public record PerguntaResponse(Long id, String texto, TipoPergunta tipo, Boolean obrigatoria, Integer ordem) {
    }

    public record PesquisaResponse(Long id, String titulo, LocalDate dataInicio, LocalDate dataFim,
                                   StatusPesquisa status, long totalPerguntas, long totalParticipacoes,
                                   Boolean jaRespondeu, Boolean abertaParaResposta, List<PerguntaResponse> perguntas) {
    }

    public record RespostaItem(@NotNull(message = "Informe a pergunta") Long perguntaId, Integer valor,
                               @Size(max = 2000) String texto) {
    }

    public record RespostasRequest(@NotNull(message = "Informe as respostas") @Valid List<RespostaItem> respostas) {
    }

    public record ResultadoSetorResponse(String setor, long totalRespostas, boolean exibido, Double media) {
    }

    public record ResultadoPerguntaResponse(Long perguntaId, Integer ordem, String texto, TipoPergunta tipo,
                                            long totalRespostas, boolean exibido, Double media,
                                            List<Long> distribuicao, List<ResultadoSetorResponse> porSetor,
                                            List<String> respostasTexto) {
    }

    public record ResultadoClimaResponse(Long pesquisaId, String titulo, StatusPesquisa status,
                                         long totalParticipacoes, int minimoRespostas,
                                         List<ResultadoPerguntaResponse> perguntas) {
    }

    // ---------- Indicadores e histórico ----------

    public record CicloResumo(Long id, String nome, StatusCiclo status) {
    }

    public record PesquisaResumo(Long id, String titulo, StatusPesquisa status, long participacoes, long elegiveis) {
    }

    public record IndicadoresResponse(String escopo, long totalColaboradores, CicloResumo cicloAtual,
                                      long autoavaliacoesConcluidas, long avaliacoesGestorConcluidas,
                                      long avaliacoesEsperadas, Map<String, Long> distribuicaoNineBox,
                                      long pendentesNineBox, long pdisAtivos, Map<String, Long> acoesPorStatus,
                                      long acoesAtrasadas, PesquisaResumo pesquisaAtual,
                                      Double mediaGeralGestor) {
    }

    public record HistoricoCicloResponse(Long cicloId, String cicloNome, LocalDate dataInicio, StatusCiclo status,
                                         boolean resultadosDisponiveis, MediasResponse gestor,
                                         MediasResponse autoavaliacao, Nivel desempenho, Nivel potencial,
                                         String quadrante) {
    }

    public record HistoricoPDIResponse(Long id, String objetivo, StatusPDI status, LocalDate criadoEm,
                                       int progressoGeral, int totalAcoes) {
    }

    public record HistoricoResponse(ColaboradorResponse colaborador, List<HistoricoCicloResponse> ciclos,
                                    List<HistoricoPDIResponse> pdis) {
    }
}
