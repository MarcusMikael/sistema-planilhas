package sdpp_backend.security;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sdpp_backend.config.SdppProperties;

/**
 * Cookie HttpOnly que carrega o JWT da sessão do navegador. Como o JavaScript da página não consegue
 * lê-lo, um eventual XSS não rouba a sessão. Clientes de API (testes, scripts) podem continuar usando
 * o cabeçalho "Authorization: Bearer".
 */
@Component
public class CookieSessao {

    public static final String NOME = "sdpp_sessao";
    /** Cabeçalho exigido em requisições que alteram dados quando a autenticação vem do cookie (anti-CSRF). */
    public static final String CABECALHO_CSRF = "X-SDPP-CSRF";

    private final SdppProperties props;

    public CookieSessao(SdppProperties props) {
        this.props = props;
    }

    public void gravar(HttpServletResponse resposta, String token) {
        escrever(resposta, token, Duration.ofHours(props.jwt().expiracaoHoras()));
    }

    public void apagar(HttpServletResponse resposta) {
        escrever(resposta, "", Duration.ZERO);
    }

    public Optional<String> ler(HttpServletRequest requisicao) {
        Cookie[] cookies = requisicao.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies).filter(c -> NOME.equals(c.getName())).map(Cookie::getValue)
                .filter(v -> !v.isBlank()).findFirst();
    }

    private void escrever(HttpServletResponse resposta, String valor, Duration validade) {
        ResponseCookie cookie = ResponseCookie.from(NOME, valor)
                .httpOnly(true)
                .secure(props.cookie().secure())
                .sameSite(props.cookie().sameSite())
                .path("/api")
                .maxAge(validade)
                .build();
        resposta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
