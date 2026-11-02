package sdpp_backend.model;

import jakarta.persistence.*;
import sdpp_backend.model.enums.Nivel;

@Entity
@Table(name = "quadrante",
        uniqueConstraints = @UniqueConstraint(columnNames = {"desempenho", "potencial"}))
public class QuadranteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel desempenho;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel potencial;

    @Column(nullable = false)
    private String rotulo;

    @Column(nullable = false, length = 500)
    private String descricao;

    // Uma ação sugerida por linha (RF-NBX-04)
    @Column(name = "plano_acao", nullable = false, length = 3000)
    private String planoAcao;

    public QuadranteModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPlanoAcao() {
        return planoAcao;
    }

    public void setPlanoAcao(String planoAcao) {
        this.planoAcao = planoAcao;
    }
}
