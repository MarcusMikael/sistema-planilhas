package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import sdpp_backend.model.enums.StatusPesquisa;

@Entity
@Table(name = "pesquisa_clima")
public class PesquisaClimaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPesquisa status = StatusPesquisa.RASCUNHO;

    public PesquisaClimaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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

    public StatusPesquisa getStatus() {
        return status;
    }

    public void setStatus(StatusPesquisa status) {
        this.status = status;
    }
}
