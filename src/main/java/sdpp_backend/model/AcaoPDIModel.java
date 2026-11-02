package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import sdpp_backend.model.enums.StatusAcaoPDI;

@Entity
@Table(name = "acao_pdi")
public class AcaoPDIModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdi_id", nullable = false)
    private PDIModel pdi;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_termino", nullable = false)
    private LocalDate dataTermino;

    @Column(length = 1000)
    private String entrega;

    // Texto livre com valor opcional, ex.: "Curso online - R$ 51,35" (PA-21)
    @Column(length = 500)
    private String recursos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAcaoPDI status = StatusAcaoPDI.A_FAZER;

    // Percentual de 0 a 100
    @Column(nullable = false)
    private Integer progresso = 0;

    public AcaoPDIModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PDIModel getPdi() {
        return pdi;
    }

    public void setPdi(PDIModel pdi) {
        this.pdi = pdi;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(LocalDate dataTermino) {
        this.dataTermino = dataTermino;
    }

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public String getRecursos() {
        return recursos;
    }

    public void setRecursos(String recursos) {
        this.recursos = recursos;
    }

    public StatusAcaoPDI getStatus() {
        return status;
    }

    public void setStatus(StatusAcaoPDI status) {
        this.status = status;
    }

    public Integer getProgresso() {
        return progresso;
    }

    public void setProgresso(Integer progresso) {
        this.progresso = progresso;
    }
}
