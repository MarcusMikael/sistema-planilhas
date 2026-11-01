package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "acao_pdi")
public class AcaoPDIModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdi_id", nullable = false)
    private PDIModel pdi;

    @Column(nullable = false)
    private String descricao;

    private String responsavel;

    private LocalDate prazo;

    @Column(nullable = false)
    private String status;

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

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
