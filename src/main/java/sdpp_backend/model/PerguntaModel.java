package sdpp_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pergunta")
public class PerguntaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pesquisa_id", nullable = false)
    private PesquisaClimaModel pesquisa;

    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Boolean obrigatoria;

    @Column(nullable = false)
    private Integer ordem;

    public PerguntaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PesquisaClimaModel getPesquisa() {
        return pesquisa;
    }

    public void setPesquisa(PesquisaClimaModel pesquisa) {
        this.pesquisa = pesquisa;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getObrigatoria() {
        return obrigatoria;
    }

    public void setObrigatoria(Boolean obrigatoria) {
        this.obrigatoria = obrigatoria;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

}
