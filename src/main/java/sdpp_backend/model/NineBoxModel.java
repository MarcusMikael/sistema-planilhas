package sdpp_backend.model;

import jakarta.persistence.*;
import sdpp_backend.model.enums.Nivel;

@Entity
@Table(name = "nine_box",
        uniqueConstraints = @UniqueConstraint(columnNames = {"colaborador_id", "ciclo_id"}))
public class NineBoxModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorModel colaborador;

    @ManyToOne
    @JoinColumn(name = "ciclo_id", nullable = false)
    private CicloAvaliacaoModel ciclo;

    // Eixos distintos (RN-03); qualquer um nulo deixa a classificação pendente
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Nivel desempenho;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Nivel potencial;

    @ManyToOne
    @JoinColumn(name = "quadrante_id")
    private QuadranteModel quadrante;

    @Column(length = 2000)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "registrado_por_id")
    private ColaboradorModel registradoPor;

    public NineBoxModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ColaboradorModel getColaborador() {
        return colaborador;
    }

    public void setColaborador(ColaboradorModel colaborador) {
        this.colaborador = colaborador;
    }

    public CicloAvaliacaoModel getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloAvaliacaoModel ciclo) {
        this.ciclo = ciclo;
    }

    public Nivel getDesempenho() {
        return desempenho;
    }

    public void setDesempenho(Nivel desempenho) {
        this.desempenho = desempenho;
    }

    public Nivel getPotencial() {
        return potencial;
    }

    public void setPotencial(Nivel potencial) {
        this.potencial = potencial;
    }

    public QuadranteModel getQuadrante() {
        return quadrante;
    }

    public void setQuadrante(QuadranteModel quadrante) {
        this.quadrante = quadrante;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public ColaboradorModel getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(ColaboradorModel registradoPor) {
        this.registradoPor = registradoPor;
    }
}
