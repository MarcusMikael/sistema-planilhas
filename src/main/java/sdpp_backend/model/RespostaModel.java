package sdpp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resposta")
public class RespostaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pergunta_id", nullable = false)
    private PerguntaModel pergunta;

    @Column(name = "codigo_sessao", nullable = false)
    private String codigoSessao;

    @Column(nullable = false)
    private String valor;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    public RespostaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PerguntaModel getPergunta() {
        return pergunta;
    }

    public void setPergunta(PerguntaModel pergunta) {
        this.pergunta = pergunta;
    }

    public String getCodigoSessao() {
        return codigoSessao;
    }

    public void setCodigoSessao(String codigoSessao) {
        this.codigoSessao = codigoSessao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public LocalDateTime getRespondidaEm() {
        return respondidaEm;
    }

    public void setRespondidaEm(LocalDateTime respondidaEm) {
        this.respondidaEm = respondidaEm;
    }

}