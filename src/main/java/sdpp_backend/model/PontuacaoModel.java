package sdpp_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pontuacao",
        uniqueConstraints = @UniqueConstraint(columnNames = {"avaliacao_id", "ciclo_competencia_id"}))
public class PontuacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private AvaliacaoModel avaliacao;

    @ManyToOne
    @JoinColumn(name = "ciclo_competencia_id", nullable = false)
    private CicloCompetenciaModel cicloCompetencia;

    // Nulo = ainda não pontuado; nunca é tratado como zero (RN-02)
    private Integer nota;

    @Column(length = 1000)
    private String comentario;

    public PontuacaoModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AvaliacaoModel getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(AvaliacaoModel avaliacao) {
        this.avaliacao = avaliacao;
    }

    public CicloCompetenciaModel getCicloCompetencia() {
        return cicloCompetencia;
    }

    public void setCicloCompetencia(CicloCompetenciaModel cicloCompetencia) {
        this.cicloCompetencia = cicloCompetencia;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
