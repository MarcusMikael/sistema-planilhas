package sdpp_backend.model;

import java.util.UUID;

import jakarta.persistence.*;

/**
 * Resposta anônima: sem colaborador, sem horário e sem código de sessão (RN-06, G3).
 * O id é um UUID aleatório para que a ordem de gravação não permita ligar a resposta à participação.
 */
@Entity
@Table(name = "resposta")
public class RespostaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pergunta_id", nullable = false)
    private PerguntaModel pergunta;

    // Departamento copiado do cadastro, usado só para agregação por setor
    private String setor;

    // Perguntas de escala (1 a 5)
    private Integer valor;

    // Perguntas abertas
    @Column(length = 2000)
    private String texto;

    public RespostaModel() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PerguntaModel getPergunta() {
        return pergunta;
    }

    public void setPergunta(PerguntaModel pergunta) {
        this.pergunta = pergunta;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
