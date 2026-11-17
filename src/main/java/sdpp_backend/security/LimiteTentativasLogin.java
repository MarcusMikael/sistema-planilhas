package sdpp_backend.security;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sdpp_backend.exception.MuitasTentativasException;
import sdpp_backend.security.RegistroTentativas.Registro;

/**
 * Limita tentativas de login com senha errada (contra força bruta), seguindo a ideia de "cookies de
 * dispositivo" da OWASP:
 * <ul>
 * <li>De um navegador desconhecido: no máximo {@value #MAX_FALHAS_CONTA} falhas por conta e
 * {@value #MAX_FALHAS_GLOBAL} no total (contra testar uma senha comum em muitas contas) a cada janela.</li>
 * <li>De um navegador onde a pessoa já entrou antes (cookie de dispositivo assinado): contador próprio, então
 * alguém errando a senha de propósito em outro lugar não bloqueia o dono da conta.</li>
 * </ul>
 * Os contadores ficam no banco e sobrevivem a reinícios do serviço.
 */
@Component
public class LimiteTentativasLogin {

    static final int MAX_FALHAS_CONTA = 5;
    static final int MAX_FALHAS_GLOBAL = 50;
    static final Duration JANELA = Duration.ofMinutes(15);
    static final String CHAVE_GLOBAL = "global";

    private final RegistroTentativas registro;
    private final Clock relogio;

    @Autowired
    public LimiteTentativasLogin(RegistroTentativas registro) {
        this(registro, Clock.systemUTC());
    }

    LimiteTentativasLogin(RegistroTentativas registro, Clock relogio) {
        this.registro = registro;
        this.relogio = relogio;
    }

    /**
     * Lança MuitasTentativasException se o login estiver bloqueado.
     *
     * @param dispositivo identificador do navegador confiável para este e-mail, ou null se desconhecido
     */
    public void verificar(String email, String dispositivo) {
        if (dispositivo != null) {
            exigirAbaixo(chaveDispositivo(email, dispositivo), MAX_FALHAS_CONTA);
            return;
        }
        exigirAbaixo(chaveConta(email), MAX_FALHAS_CONTA);
        exigirAbaixo(CHAVE_GLOBAL, MAX_FALHAS_GLOBAL);
    }

    public void registrarFalha(String email, String dispositivo) {
        registro.apagarAnterioresA(relogio.instant().minus(JANELA));
        List<String> chaves = dispositivo != null ? List.of(chaveDispositivo(email, dispositivo))
                : List.of(chaveConta(email), CHAVE_GLOBAL);
        for (String chave : chaves) {
            Registro atual = registro.buscar(chave).filter(r -> !expirado(r)).orElse(null);
            registro.gravar(chave, atual == null ? new Registro(1, relogio.instant())
                    : new Registro(atual.falhas() + 1, atual.inicio()));
        }
    }

    public void registrarSucesso(String email, String dispositivo) {
        registro.apagar(chaveConta(email));
        if (dispositivo != null) {
            registro.apagar(chaveDispositivo(email, dispositivo));
        }
    }

    private void exigirAbaixo(String chave, int maximo) {
        registro.buscar(chave).filter(r -> !expirado(r) && r.falhas() >= maximo).ifPresent(r -> {
            long minutos = Math.max(1, Duration.between(relogio.instant(), r.inicio().plus(JANELA)).toMinutes() + 1);
            throw new MuitasTentativasException("Muitas tentativas de login sem sucesso. Tente novamente em "
                    + minutos + " minuto(s).");
        });
    }

    private boolean expirado(Registro r) {
        return relogio.instant().isAfter(r.inicio().plus(JANELA));
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String chaveConta(String email) {
        return "conta:" + normalizar(email);
    }

    private static String chaveDispositivo(String email, String dispositivo) {
        return "dispositivo:" + normalizar(email) + "|" + dispositivo;
    }
}
