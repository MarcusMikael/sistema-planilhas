package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "colaborador")
public class ColaboradorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    private CargoModel cargo;

    @ManyToOne
    @JoinColumn(name = "departamento_id", nullable = false)
    private DepartamentoModel departamento;

    // Gestor responsável: define a equipe (PRE-03, RF-SEG-02)
    @ManyToOne
    @JoinColumn(name = "gestor_id")
    private ColaboradorModel gestor;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private UsuarioModel usuario;

    public ColaboradorModel() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public CargoModel getCargo() {
        return cargo;
    }

    public void setCargo(CargoModel cargo) {
        this.cargo = cargo;
    }

    public DepartamentoModel getDepartamento() {
        return departamento;
    }

    public void setDepartamento(DepartamentoModel departamento) {
        this.departamento = departamento;
    }

    public ColaboradorModel getGestor() {
        return gestor;
    }

    public void setGestor(ColaboradorModel gestor) {
        this.gestor = gestor;
    }

    public UsuarioModel getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioModel usuario) {
        this.usuario = usuario;
    }
}
