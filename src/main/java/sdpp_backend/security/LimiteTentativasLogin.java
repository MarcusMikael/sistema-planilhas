package sdpp_backend.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import sdpp_backend.exception.MuitasTentativasException;

/**
 * Limita tentativas de login com senha errada por conta, contra força bruta (RNF-SEG).
 * Depois de MAX_FALHAS falhas dentro de JANELA, novas tentativas para o mesmo e-mail são
 * recusadas até a janela expirar. Um login correto zera o contador.
 * O controle fica em memória: suficiente para uma instância; com várias, use um armazenamento compartilhado.
 */
@Component
public class LimiteTentativasLogin {

    static final int MAX_FALHAS = 5;
    static final Duration JANELA = Duration.ofMinutes(15);
    private static final int LIMPAR_ACIMA_DE = 10_000;

    private record Registro(int falhas, Instant inicio) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Clock relogio;

    public LimiteTentativasLogin() {
        this(Clock.systemUTC());
    }

    LimiteTentativasLogin(Clock relogio) {
        this.relogio = relogio;
    }

    /** Lança MuitasTentativasException se o e-mail estiver bloqueado. */
    public void verificar(String email) {
        Registro r = registros.get(chave(email));
        if (r != null && !expirado(r) && r.falhas() >= MAX_FALHAS) {
            long minutos = Math.max(1, Duration.between(relogio.instant(), r.inicio().plus(JANELA)).toMinutes() + 1);
            throw new MuitasTentativasException("Muitas tentativas de login sem sucesso. Tente novamente em "
                    + minutos + " minuto(s).");
        }
    }

    public void registrarFalha(String email) {
        if (registros.size() > LIMPAR_ACIMA_DE) {
            registros.values().removeIf(this::expirado);
        }
        registros.compute(chave(email), (k, r) -> r == null || expirado(r)
                ? new Registro(1, relogio.instant())
                : new Registro(r.falhas() + 1, r.inicio()));
    }

    public void registrarSucesso(String email) {
        registros.remove(chave(email));
    }

    private boolean expirado(Registro r) {
        return relogio.instant().isAfter(r.inicio().plus(JANELA));
    }

    private static String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
