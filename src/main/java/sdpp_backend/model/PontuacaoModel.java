package sdpp_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pontuacao")
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

    private Double nota;

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

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

}
