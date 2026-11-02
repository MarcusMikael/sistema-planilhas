package sdpp_backend.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Estrutura única de erro (Arquitetura 5.6): status, erro, mensagem e campos inválidos,
 * sem expor detalhes internos, senhas ou dados pessoais.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErroResponse(int status, String erro, String mensagem,
                               Map<String, String> campos, LocalDateTime timestamp) {
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem, Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, campos, LocalDateTime.now()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> credenciais(CredenciaisInvalidasException ex) {
        return erro(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return erro(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErroResponse> acessoNegado(RuntimeException ex) {
        String msg = ex instanceof AcessoNegadoException ? ex.getMessage()
                : "Você não tem permissão para realizar esta operação.";
        return erro(HttpStatus.FORBIDDEN, msg, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição.", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou com valores fora do domínio.", null);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErroResponse> parametroInvalido(Exception ex) {
        return erro(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou inválido.", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return erro(HttpStatus.CONFLICT,
                "Operação viola a integridade dos dados (registro duplicado ou em uso).", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex) {
        return erro(HttpStatus.NOT_FOUND, "Recurso não encontrado.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodo(HttpRequestMethodNotSupportedException ex) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado nesta rota.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", null);
    }
}
