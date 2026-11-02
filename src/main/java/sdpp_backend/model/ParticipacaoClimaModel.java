package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Registra QUEM respondeu (sem conteúdo), para garantir uma resposta por campanha (RN-10).
 * O conteúdo fica em {@link RespostaModel}, sem vínculo com o colaborador (RF-CLI-03, PA-16).
 */
@Entity
@Table(name = "participacao_clima",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pesquisa_id", "colaborador_id"}))
public class ParticipacaoClimaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pesquisa_id", nullable = false)
    private PesquisaClimaModel pesquisa;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorModel colaborador;

    // Só a data, sem hora, para dificultar correlação com as respostas (G3)
    @Column(name = "data_participacao", nullable = false)
    private LocalDate dataParticipacao;

    public ParticipacaoClimaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PesquisaClimaModel getPesquisa() {
        return pesquisa;
    }

    public void setPesquisa(PesquisaClimaModel pesquisa) {
        this.pesquisa = pesquisa;
    }

    public ColaboradorModel getColaborador() {
        return colaborador;
    }

    public void setColaborador(ColaboradorModel colaborador) {
        this.colaborador = colaborador;
    }

    public LocalDate getDataParticipacao() {
        return dataParticipacao;
    }

    public void setDataParticipacao(LocalDate dataParticipacao) {
        this.dataParticipacao = dataParticipacao;
    }
}
