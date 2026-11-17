package sdpp_backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contador de falhas de login de uma chave (conta, conta + dispositivo ou global) dentro de uma janela de tempo.
 * Fica no banco para não zerar quando o serviço reinicia (o plano gratuito do Render desliga sem uso).
 */
@Entity
@Table(name = "tentativa_login")
public class TentativaLoginModel {

    @Id
    @Column(length = 400)
    private String chave;

    @Column(nullable = false)
    private int falhas;

    @Column(nullable = false)
    private Instant inicio;

    public TentativaLoginModel() {
    }

    public TentativaLoginModel(String chave, int falhas, Instant inicio) {
        this.chave = chave;
        this.falhas = falhas;
        this.inicio = inicio;
    }

    public String getChave() {
        return chave;
    }

    public int getFalhas() {
        return falhas;
    }

    public void setFalhas(int falhas) {
        this.falhas = falhas;
    }

    public Instant getInicio() {
        return inicio;
    }

    public void setInicio(Instant inicio) {
        this.inicio = inicio;
    }
}
