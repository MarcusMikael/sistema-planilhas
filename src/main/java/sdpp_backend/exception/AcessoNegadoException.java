package sdpp_backend.exception;

/** Operação não permitida para o usuário autenticado (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
