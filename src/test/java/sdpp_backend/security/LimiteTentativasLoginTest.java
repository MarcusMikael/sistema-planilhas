package sdpp_backend.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    /** Armazenamento em memória no lugar do banco. */
    private static final class EmMemoria implements RegistroTentativas {
        final Map<String, Registro> dados = new HashMap<>();

        @Override
        public Optional<Registro> buscar(String chave) {
            return Optional.ofNullable(dados.get(chave));
        }

        @Override
        public void gravar(String chave, Registro registro) {
            dados.put(chave, registro);
        }

        @Override
        public void apagar(String chave) {
            dados.remove(chave);
        }

        @Override
        public void apagarAnterioresA(Instant limite) {
            dados.values().removeIf(r -> r.inicio().isBefore(limite));
        }
    }

    private final Relogio relogio = new Relogio();
    private final EmMemoria memoria = new EmMemoria();
    private final LimiteTentativasLogin limite = new LimiteTentativasLogin(memoria, relogio);

    private void falhar(String email, String dispositivo, int vezes) {
        for (int i = 0; i < vezes; i++) {
            limite.registrarFalha(email, dispositivo);
        }
    }

    @Test
    void bloqueiaContaDepoisDoLimiteSemDiferenciarMaiusculas() {
        falhar("Ana@sdpp.local", null, LimiteTentativasLogin.MAX_FALHAS_CONTA - 1);
        assertThatCode(() -> limite.verificar("ana@sdpp.local", null)).doesNotThrowAnyException();
        limite.registrarFalha(" ANA@sdpp.local ", null);
        assertThatThrownBy(() -> limite.verificar("ana@sdpp.local", null)).isInstanceOf(MuitasTentativasException.class);
        assertThatCode(() -> limite.verificar("outra@sdpp.local", null)).doesNotThrowAnyException();
    }

    @Test
    void dispositivoConfiavelNaoEBloqueadoPorFalhasDeOutrosLugares() {
        falhar("rh@sdpp.local", null, LimiteTentativasLogin.MAX_FALHAS_CONTA);
        assertThatThrownBy(() -> limite.verificar("rh@sdpp.local", null)).isInstanceOf(MuitasTentativasException.class);
        // O dono da conta, no navegador onde já entrou, continua podendo entrar
        assertThatCode(() -> limite.verificar("rh@sdpp.local", "meu-navegador")).doesNotThrowAnyException();
    }

    @Test
    void dispositivoConfiavelTambemTemLimiteProprio() {
        falhar("rh@sdpp.local", "meu-navegador", LimiteTentativasLogin.MAX_FALHAS_CONTA);
        assertThatThrownBy(() -> limite.verificar("rh@sdpp.local", "meu-navegador"))
                .isInstanceOf(MuitasTentativasException.class);
        assertThatCode(() -> limite.verificar("rh@sdpp.local", null)).doesNotThrowAnyException();
    }

    @Test
    void limiteGlobalContraSenhaComumEmMuitasContas() {
        for (int i = 0; i < LimiteTentativasLogin.MAX_FALHAS_GLOBAL; i++) {
            limite.registrarFalha("pessoa" + i + "@sdpp.local", null);
        }
        assertThatThrownBy(() -> limite.verificar("nova@sdpp.local", null)).isInstanceOf(MuitasTentativasException.class);
        assertThatCode(() -> limite.verificar("nova@sdpp.local", "navegador-conhecido")).doesNotThrowAnyException();
    }

    @Test
    void desbloqueiaQuandoAJanelaExpira() {
        falhar("ana@sdpp.local", null, LimiteTentativasLogin.MAX_FALHAS_CONTA);
        relogio.agora = relogio.agora.plus(LimiteTentativasLogin.JANELA).plus(Duration.ofSeconds(1));
        assertThatCode(() -> limite.verificar("ana@sdpp.local", null)).doesNotThrowAnyException();
    }

    @Test
    void sucessoZeraContadorDaConta() {
        falhar("ana@sdpp.local", null, LimiteTentativasLogin.MAX_FALHAS_CONTA - 1);
        limite.registrarSucesso("ana@sdpp.local", null);
        limite.registrarFalha("ana@sdpp.local", null);
        assertThatCode(() -> limite.verificar("ana@sdpp.local", null)).doesNotThrowAnyException();
    }
}
