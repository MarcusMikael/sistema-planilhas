package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import sdpp_backend.model.enums.StatusPDI;

@Entity
@Table(name = "pdi")
public class PDIModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorModel colaborador;

    @ManyToOne
    @JoinColumn(name = "gestor_id")
    private ColaboradorModel gestor;

    // Vínculo opcional com ciclo (G7)
    @ManyToOne
    @JoinColumn(name = "ciclo_id")
    private CicloAvaliacaoModel ciclo;

    @Column(name = "cargo_desejado")
    private String cargoDesejado;

    @Column(nullable = false, length = 2000)
    private String objetivo;

    @Column(name = "habilidades_desenvolver", length = 2000)
    private String habilidadesDesenvolver;

    @Column(name = "habilidades_sucesso", length = 2000)
    private String habilidadesSucesso;

    @Column(length = 4000)
    private String anotacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPDI status = StatusPDI.ATIVO;

    @Column(name = "criado_em", nullable = false)
    private LocalDate criadoEm;

    public PDIModel() {
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

    public ColaboradorModel getGestor() {
        return gestor;
    }

    public void setGestor(ColaboradorModel gestor) {
        this.gestor = gestor;
    }

    public CicloAvaliacaoModel getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloAvaliacaoModel ciclo) {
        this.ciclo = ciclo;
    }

    public String getCargoDesejado() {
        return cargoDesejado;
    }

    public void setCargoDesejado(String cargoDesejado) {
        this.cargoDesejado = cargoDesejado;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getHabilidadesDesenvolver() {
        return habilidadesDesenvolver;
    }

    public void setHabilidadesDesenvolver(String habilidadesDesenvolver) {
        this.habilidadesDesenvolver = habilidadesDesenvolver;
    }

    public String getHabilidadesSucesso() {
        return habilidadesSucesso;
    }

    public void setHabilidadesSucesso(String habilidadesSucesso) {
        this.habilidadesSucesso = habilidadesSucesso;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public StatusPDI getStatus() {
        return status;
    }

    public void setStatus(StatusPDI status) {
        this.status = status;
    }

    public LocalDate getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }
}
