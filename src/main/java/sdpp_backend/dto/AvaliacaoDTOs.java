package sdpp_backend.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sdpp_backend.dto.CadastroDTOs.CompetenciaResponse;
import sdpp_backend.model.enums.Nivel;
import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.StatusCiclo;
import sdpp_backend.model.enums.TipoAvaliacao;
import sdpp_backend.model.enums.TipoCompetencia;

/** DTOs dos módulos CIC, AVA e NBX. */
public final class AvaliacaoDTOs {

    private AvaliacaoDTOs() {
    }

    // ---------- Ciclos ----------

    public record CicloRequest(
            @NotBlank(message = "Informe o nome do ciclo") @Size(max = 150) String nome,
            @NotNull(message = "Informe a data de início") LocalDate dataInicio,
            @NotNull(message = "Informe a data de término") LocalDate dataFim) {
    }

    public record CicloResponse(Long id, String nome, LocalDate dataInicio, LocalDate dataFim, StatusCiclo status,
                                long qtdComportamentais, long qtdTecnicas, List<CompetenciaCicloResponse> competencias) {
    }

    public record CompetenciaCicloResponse(Long cicloCompetenciaId, CompetenciaResponse competencia) {
    }

    public record SelecaoCompetenciasRequest(@NotNull(message = "Informe as competências") List<Long> competenciaIds) {
    }

    // ---------- Avaliações ----------

    public record AbrirAvaliacaoRequest(
            @NotNull(message = "Informe o ciclo") Long cicloId,
            @NotNull(message = "Informe o colaborador avaliado") Long colaboradorId,
            @NotNull(message = "Informe o tipo") TipoAvaliacao tipo) {
    }

    public record NotaItem(
            @NotNull(message = "Informe a competência") Long cicloCompetenciaId,
            @Min(value = 0, message = "A nota deve estar entre 0 e 5")
            @Max(value = 5, message = "A nota deve estar entre 0 e 5") Integer nota,
            @Size(max = 1000) String comentario) {
    }

    public record NotasRequest(@NotNull(message = "Informe as notas") @Valid List<NotaItem> notas) {
    }

    public record ItemAvaliacaoResponse(Long cicloCompetenciaId, Long competenciaId, String nome, String descricao,
                                        TipoCompetencia tipo, Integer nota, String comentario) {
    }

    public record AvaliacaoResponse(Long id, Long cicloId, String cicloNome, StatusCiclo cicloStatus,
                                    Long colaboradorId, String colaboradorNome, String colaboradorCargo,
                                    Long avaliadorId, String avaliadorNome, String avaliadorCargo,
                                    TipoAvaliacao tipo, StatusAvaliacao status, LocalDate dataAvaliacao,
                                    int notasPreenchidas, int totalCompetencias,
                                    List<ItemAvaliacaoResponse> itens) {
    }

    /** Avaliação que o usuário atual deve realizar (autoavaliação ou avaliação da equipe). */
    public record PendenciaResponse(Long cicloId, String cicloNome, LocalDate cicloDataFim, Long colaboradorId,
                                    String colaboradorNome, String colaboradorCargo, TipoAvaliacao tipo,
                                    Long avaliacaoId, StatusAvaliacao status) {
    }

    // ---------- Resultados ----------

    public record MediasResponse(StatusAvaliacao status, Double comportamental, Double tecnica, Double geral) {
    }

    public record ItemComparacaoResponse(String competencia, TipoCompetencia tipo, Integer notaGestor,
                                         Integer notaAutoavaliacao) {
    }

    public record ResultadoColaboradorResponse(Long cicloId, String cicloNome, StatusCiclo cicloStatus,
                                               Long colaboradorId, String colaboradorNome, String cargo,
                                               String departamento, MediasResponse gestor,
                                               MediasResponse autoavaliacao, List<ItemComparacaoResponse> itens) {
    }

    public record ColaboradorRef(Long id, String nome) {
    }

    public record LinhaMapaResponse(Long competenciaId, String competencia, List<Integer> notasGestor,
                                    Double mediaGestor, Double mediaAutoavaliacao) {
    }

    /** Relatório "mapa de competências" de [P1]: competência × colaborador (RF-IND-04). */
    public record MapaCompetenciasResponse(Long cicloId, String cicloNome, List<ColaboradorRef> colaboradores,
                                           List<LinhaMapaResponse> comportamentais, List<LinhaMapaResponse> tecnicas) {
    }

    // ---------- Nine Box ----------

    public record QuadranteResponse(Long id, Nivel desempenho, Nivel potencial, String rotulo, String descricao,
                                    List<String> acoes) {
    }

    public record QuadranteRequest(
            @NotBlank(message = "Informe o rótulo") @Size(max = 100) String rotulo,
            @NotBlank(message = "Informe a descrição") @Size(max = 500) String descricao,
            @NotNull(message = "Informe as ações sugeridas") List<@NotBlank String> acoes) {
    }

    public record NineBoxRequest(
            @NotNull(message = "Informe o ciclo") Long cicloId,
            @NotNull(message = "Informe o colaborador") Long colaboradorId,
            Nivel desempenho,
            Nivel potencial,
            @Size(max = 2000) String observacoes) {
    }

    public record NineBoxItemResponse(Long colaboradorId, String colaboradorNome, String cargo, String departamento,
                                      Long gestorId, Long registroId, Nivel desempenho, Nivel potencial, Double mediaGestor,
                                      Nivel desempenhoSugerido, boolean pendente, QuadranteResponse quadrante,
                                      String observacoes) {
    }

    public record NineBoxResponse(Long cicloId, String cicloNome, StatusCiclo cicloStatus,
                                  List<NineBoxItemResponse> itens, List<QuadranteResponse> quadrantes) {
    }
}
