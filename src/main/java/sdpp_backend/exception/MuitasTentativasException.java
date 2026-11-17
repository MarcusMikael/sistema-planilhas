package sdpp_backend.exception;

/** Excesso de tentativas de login sem sucesso (HTTP 429). */
public class MuitasTentativasException extends RuntimeException {

    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
