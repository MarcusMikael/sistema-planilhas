package sdpp_backend.service.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgrupamentoSetoresTest {

    private static final int MINIMO = 5;
    private static final String DEMAIS = AgrupamentoSetores.GRUPO_DEMAIS;

    private static Map<String, List<Integer>> setores(Object... nomeEQuantidade) {
        Map<String, List<Integer>> m = new LinkedHashMap<>();
        for (int i = 0; i < nomeEQuantidade.length; i += 2) {
            m.put((String) nomeEQuantidade[i], Collections.nCopies((Integer) nomeEQuantidade[i + 1], 1));
        }
        return m;
    }

    private static Map<String, Integer> tamanhos(Map<String, List<Integer>> grupos) {
        Map<String, Integer> m = new LinkedHashMap<>();
        grupos.forEach((k, v) -> m.put(k, v.size()));
        return m;
    }

    @Test
    @DisplayName("Todos os setores acima do mínimo são exibidos separadamente, em ordem alfabética")
    void todosVisiveis() {
        assertThat(tamanhos(AgrupamentoSetores.agrupar(setores("TI", 6, "Comercial", 5), MINIMO)))
                .containsExactly(Map.entry("Comercial", 5), Map.entry("TI", 6));
    }

    @Test
    @DisplayName("Setores pequenos que somam o mínimo formam um grupo próprio")
    void pequenosSomamMinimo() {
        assertThat(tamanhos(AgrupamentoSetores.agrupar(setores("TI", 6, "A", 2, "B", 3), MINIMO)))
                .containsExactly(Map.entry("TI", 6), Map.entry(DEMAIS, 5));
    }

    @Test
    @DisplayName("Supressão complementar: grupo pequeno absorve o menor setor visível")
    void supressaoComplementar() {
        assertThat(tamanhos(AgrupamentoSetores.agrupar(setores("TI", 8, "RH", 5, "Comercial", 1), MINIMO)))
                .containsExactly(Map.entry("TI", 8), Map.entry(DEMAIS, 6));
    }

    @Test
    @DisplayName("Caso do ataque: 5 + 1 viram um único grupo de 6")
    void casoDoAtaque() {
        assertThat(tamanhos(AgrupamentoSetores.agrupar(setores("Tecnologia", 5, "Comercial", 1), MINIMO)))
                .containsExactly(Map.entry(DEMAIS, 6));
    }

    @Test
    @DisplayName("Total abaixo do mínimo: nenhum grupo é exibido")
    void totalAbaixoDoMinimo() {
        assertThat(AgrupamentoSetores.agrupar(setores("TI", 3, "RH", 1), MINIMO)).isEmpty();
        assertThat(AgrupamentoSetores.agrupar(Map.<String, List<Integer>>of(), MINIMO)).isEmpty();
    }

    @Test
    @DisplayName("Todo grupo exibido tem pelo menos o mínimo")
    void invariante() {
        for (int a = 0; a <= 7; a++) {
            for (int b = 0; b <= 7; b++) {
                for (int c = 0; c <= 7; c++) {
                    var grupos = AgrupamentoSetores.agrupar(setores("A", a, "B", b, "C", c), MINIMO);
                    int total = grupos.values().stream().mapToInt(List::size).sum();
                    assertThat(total).isIn(0, a + b + c);
                    grupos.values().forEach(g -> assertThat(g.size()).isGreaterThanOrEqualTo(MINIMO));
                }
            }
        }
    }
}
