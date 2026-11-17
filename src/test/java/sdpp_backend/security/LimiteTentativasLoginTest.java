package sdpp_backend.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import sdpp_backend.exception.MuitasTentativasException;

class LimiteTentativasLoginTest {

    /** Relógio controlável para simular a passagem do tempo. */
    private static final class Relogio extends Clock {
        Instant agora = Instant.parse("2026-01-01T12:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final Relogio relogio = new Relogio();
    private final LimiteTentativasLogin limite = new LimiteTentativasLogin(relogio);

    private void falhar(String email, int vezes) {
        for (int i = 0; i < vezes; i++) {
            limite.registrarFalha(email);
        }
    }

    @Test
    void bloqueiaDepoisDoLimiteSemDiferenciarMaiusculas() {
        falhar("Ana@sdpp.local", LimiteTentativasLogin.MAX_FALHAS - 1);
        assertThatCode(() -> limite.verificar("ana@sdpp.local")).doesNotThrowAnyException();
        limite.registrarFalha(" ANA@sdpp.local ");
        assertThatThrownBy(() -> limite.verificar("ana@sdpp.local")).isInstanceOf(MuitasTentativasException.class);
        assertThatCode(() -> limite.verificar("outra@sdpp.local")).doesNotThrowAnyException();
    }

    @Test
    void desbloqueiaQuandoAJanelaExpira() {
        falhar("ana@sdpp.local", LimiteTentativasLogin.MAX_FALHAS);
        relogio.agora = relogio.agora.plus(LimiteTentativasLogin.JANELA).plus(Duration.ofSeconds(1));
        assertThatCode(() -> limite.verificar("ana@sdpp.local")).doesNotThrowAnyException();
    }

    @Test
    void sucessoZeraContador() {
        falhar("ana@sdpp.local", LimiteTentativasLogin.MAX_FALHAS - 1);
        limite.registrarSucesso("ana@sdpp.local");
        limite.registrarFalha("ana@sdpp.local");
        assertThatCode(() -> limite.verificar("ana@sdpp.local")).doesNotThrowAnyException();
    }
}
