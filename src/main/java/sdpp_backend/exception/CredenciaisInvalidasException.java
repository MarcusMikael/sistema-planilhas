package sdpp_backend.exception;

/** E-mail ou senha inválidos, ou conta inativa (HTTP 401). */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
