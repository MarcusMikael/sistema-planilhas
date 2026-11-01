package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "avaliacao")
public class AvaliacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorModel colaborador;

    @ManyToOne
    @JoinColumn(name = "ciclo_id", nullable = false)
    private CicloAvaliacaoModel ciclo;

    @ManyToOne
    @JoinColumn(name = "avaliador_id", nullable = false)
    private ColaboradorModel avaliador;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String status;

    @Column(name = "data_avaliacao")
    private LocalDate dataAvaliacao;

    public AvaliacaoModel() {
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

    public ColaboradorModel getAvaliador() {
        return avaliador;
    }

    public void setAvaliador(ColaboradorModel avaliador) {
        this.avaliador = avaliador;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    
}