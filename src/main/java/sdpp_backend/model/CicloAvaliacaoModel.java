package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import sdpp_backend.model.enums.StatusCiclo;

@Entity
@Table(name = "ciclo_avaliacao")
public class CicloAvaliacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusCiclo status = StatusCiclo.RASCUNHO;

    public CicloAvaliacaoModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public StatusCiclo getStatus() {
        return status;
    }

    public void setStatus(StatusCiclo status) {
        this.status = status;
    }
}
