package sdpp_backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import sdpp_backend.config.SdppProperties;
import tools.jackson.databind.json.JsonMapper;

/**
 * Emissão e validação de tokens JWT assinados com HMAC-SHA256 (AD-08).
 * O token carrega apenas o id do usuário; perfil e vínculo são relidos do banco a cada
 * requisição, para que desativações e mudanças de perfil tenham efeito imediato.
 */
@Service
public class JwtService {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER = ENCODER.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private final byte[] segredo;
    private final long expiracaoSegundos;
    private final JsonMapper json = JsonMapper.builder().build();

    public JwtService(SdppProperties props) {
        this.segredo = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (segredo.length < 32) {
            throw new IllegalStateException("sdpp.jwt.secret deve ter pelo menos 32 caracteres");
        }
        this.expiracaoSegundos = props.jwt().expiracaoHoras() * 3600L;
    }

    public String gerar(Long usuarioId) {
        long agora = Instant.now().getEpochSecond();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", usuarioId.toString());
        claims.put("iat", agora);
        claims.put("exp", agora + expiracaoSegundos);
        String payload = ENCODER.encodeToString(json.writeValueAsBytes(claims));
        String conteudo = HEADER + "." + payload;
        return conteudo + "." + assinar(conteudo);
    }

    /** Retorna o id do usuário se o token for válido e não expirado; caso contrário, null. */
    public Long validar(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3 || !partes[0].equals(HEADER)) {
                return null;
            }
            String esperada = assinar(partes[0] + "." + partes[1]);
            if (!MessageDigest.isEqual(esperada.getBytes(StandardCharsets.US_ASCII),
                    partes[2].getBytes(StandardCharsets.US_ASCII))) {
                return null;
            }
            Map<?, ?> claims = json.readValue(DECODER.decode(partes[1]), Map.class);
            long exp = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                return null;
            }
            return Long.valueOf((String) claims.get("sub"));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo, "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar token", e);
        }
    }
}
