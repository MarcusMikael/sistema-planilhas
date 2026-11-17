package sdpp_backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    /** Marca o navegador onde a pessoa já entrou com sucesso (ver LimiteTentativasLogin). */
    public static final String NOME_DISPOSITIVO = "sdpp_dispositivo";
    private static final Duration VALIDADE_DISPOSITIVO = Duration.ofDays(180);
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

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

    // ---------- Dispositivo confiável (LimiteTentativasLogin) ----------

    /**
     * Grava (ou renova) o cookie que marca este navegador como confiável para o e-mail que acabou de entrar.
     * Formato: base64url(e-mail) . id aleatório . assinatura HMAC. Só o backend consegue gerar um válido.
     */
    public void gravarDispositivo(HttpServletRequest requisicao, HttpServletResponse resposta, String email) {
        String id = lerDispositivo(requisicao, email);
        if (id == null) {
            byte[] bytes = new byte[16];
            ALEATORIO.nextBytes(bytes);
            id = B64.encodeToString(bytes);
        }
        String conteudo = B64.encodeToString(normalizar(email).getBytes(StandardCharsets.UTF_8)) + "." + id;
        ResponseCookie cookie = ResponseCookie.from(NOME_DISPOSITIVO, conteudo + "." + assinar(conteudo))
                .httpOnly(true)
                .secure(props.cookie().secure())
                .sameSite(props.cookie().sameSite())
                .path("/api/auth")
                .maxAge(VALIDADE_DISPOSITIVO)
                .build();
        resposta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Id do dispositivo se o cookie for autêntico e pertencer a este e-mail; caso contrário, null. */
    public String lerDispositivo(HttpServletRequest requisicao, String email) {
        Cookie[] cookies = requisicao.getCookies();
        if (cookies == null || email == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (!NOME_DISPOSITIVO.equals(c.getName())) {
                continue;
            }
            String[] partes = c.getValue().split("\\.");
            if (partes.length != 3) {
                return null;
            }
            String conteudo = partes[0] + "." + partes[1];
            boolean assinaturaOk = MessageDigest.isEqual(assinar(conteudo).getBytes(StandardCharsets.US_ASCII),
                    partes[2].getBytes(StandardCharsets.US_ASCII));
            String emailCookie;
            try {
                emailCookie = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                return null;
            }
            return assinaturaOk && emailCookie.equals(normalizar(email)) ? partes[1] : null;
        }
        return null;
    }

    private String assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(("dispositivo:" + props.jwt().secret()).getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            return B64.encodeToString(mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar o cookie de dispositivo", e);
        }
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
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
