package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.TipoAvaliacao;

@Entity
@Table(name = "avaliacao",
        uniqueConstraints = @UniqueConstraint(columnNames = {"colaborador_id", "ciclo_id", "tipo"}))
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAvaliacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAvaliacao status = StatusAvaliacao.EM_ANDAMENTO;

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

    public TipoAvaliacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoAvaliacao tipo) {
        this.tipo = tipo;
    }

    public StatusAvaliacao getStatus() {
        return status;
    }

    public void setStatus(StatusAvaliacao status) {
        this.status = status;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}
