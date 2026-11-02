package sdpp_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ciclo_competencia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ciclo_id", "competencia_id"}))
public class CicloCompetenciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ciclo_id", nullable = false)
    private CicloAvaliacaoModel ciclo;

    @ManyToOne
    @JoinColumn(name = "competencia_id", nullable = false)
    private CompetenciaModel competencia;

    public CicloCompetenciaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CicloAvaliacaoModel getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloAvaliacaoModel ciclo) {
        this.ciclo = ciclo;
    }

    public CompetenciaModel getCompetencia() {
        return competencia;
    }

    public void setCompetencia(CompetenciaModel competencia) {
        this.competencia = competencia;
    }
}
