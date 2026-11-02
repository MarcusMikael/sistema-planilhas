package sdpp_backend.exception;

/** Duplicidade ou estado conflitante (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
