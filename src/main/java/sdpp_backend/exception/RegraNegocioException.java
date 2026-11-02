package sdpp_backend.exception;

/** Violação de regra de negócio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
