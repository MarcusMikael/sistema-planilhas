package sdpp_backend.service.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sdpp_backend.model.enums.Nivel;

/** Casos dourados da Seção 6.1 do DRS. */
class CalculoMediasTest {

    @Test
    @DisplayName("CA-RF-AVA-04a: notas 4, 5 e 3 → média 4,0")
    void mediaDaCompetencia() {
        assertThat(CalculoMedias.media(List.of(4, 5, 3))).isEqualTo(4.0);
    }

    @Test
    @DisplayName("CA-RF-AVA-04b: nota ausente é ignorada, não vale zero → 4,5")
    void notaAusenteNaoContaComoZero() {
        assertThat(CalculoMedias.media(Arrays.asList(4, 5, null))).isEqualTo(4.5);
    }

    @Test
    @DisplayName("Sem nenhuma nota válida a média é indefinida")
    void semNotas() {
        assertThat(CalculoMedias.media(Arrays.asList(null, null))).isNull();
    }

    @Test
    @DisplayName("CA-RF-AVA-08a: gestor → comportamental 4,0; técnica 4,2; geral 4,1")
    void mediasDoColaboradorGestor() {
        assertThat(CalculoMedias.media(List.of(4, 5, 3, 4, 4))).isEqualTo(4.0);
        assertThat(CalculoMedias.media(List.of(5, 3, 4, 4, 5))).isEqualTo(4.2);
        assertThat(CalculoMedias.media(List.of(4, 5, 3, 4, 4, 5, 3, 4, 4, 5))).isEqualTo(4.1);
    }

    @Test
    @DisplayName("CA-RF-AVA-08b: autoavaliação → comportamental 4,0; técnica 3,8; geral 3,9")
    void mediasDoColaboradorAutoavaliacao() {
        assertThat(CalculoMedias.media(List.of(4, 4, 4, 4, 4))).isEqualTo(4.0);
        assertThat(CalculoMedias.media(List.of(3, 3, 4, 4, 5))).isEqualTo(3.8);
        assertThat(CalculoMedias.media(List.of(4, 4, 4, 4, 4, 3, 3, 4, 4, 5))).isEqualTo(3.9);
    }

    @Test
    @DisplayName("CA-RF-IND-03: respostas 4, 5, 3, 4, 4 → 4,0")
    void mediaClima() {
        assertThat(CalculoMedias.media(List.of(4, 5, 3, 4, 4))).isEqualTo(4.0);
    }

    @Test
    @DisplayName("PA-03: sugestão de desempenho pelas faixas configuradas")
    void sugestaoDeDesempenho() {
        assertThat(ClassificacaoNineBox.sugerirDesempenho(2.49, 2.5, 3.75)).isEqualTo(Nivel.BAIXO);
        assertThat(ClassificacaoNineBox.sugerirDesempenho(2.5, 2.5, 3.75)).isEqualTo(Nivel.MEDIO);
        assertThat(ClassificacaoNineBox.sugerirDesempenho(3.74, 2.5, 3.75)).isEqualTo(Nivel.MEDIO);
        assertThat(ClassificacaoNineBox.sugerirDesempenho(3.75, 2.5, 3.75)).isEqualTo(Nivel.ALTO);
        assertThat(ClassificacaoNineBox.sugerirDesempenho(null, 2.5, 3.75)).isNull();
    }

    @Test
    @DisplayName("CA-RF-NBX-02c: sem um dos eixos a classificação fica pendente")
    void pendente() {
        assertThat(ClassificacaoNineBox.pendente(Nivel.ALTO, null)).isTrue();
        assertThat(ClassificacaoNineBox.pendente(null, Nivel.ALTO)).isTrue();
        assertThat(ClassificacaoNineBox.pendente(Nivel.ALTO, Nivel.ALTO)).isFalse();
    }
}
