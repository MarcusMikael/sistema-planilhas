package sdpp_backend.service.calculo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Agrupamento de setores para resultados anônimos da pesquisa de clima (RN-06, PA-06).
 *
 * Não basta ocultar os setores com menos respostas que o mínimo: como o total da pergunta é exibido,
 * o valor de um setor oculto pode ser obtido por subtração (total menos os setores visíveis).
 * Por isso os setores pequenos são reunidos em um grupo único e, se esse grupo ainda ficar abaixo do
 * mínimo, os menores setores visíveis entram nele até que o grupo atinja o mínimo
 * (supressão complementar). Assim todo grupo exibido tem pelo menos o mínimo de respostas
 * e nenhuma subtração isola um grupo menor.
 */
public final class AgrupamentoSetores {

    public static final String GRUPO_DEMAIS = "Demais setores (agrupados)";

    private AgrupamentoSetores() {
    }

    /**
     * @param porSetor respostas de uma pergunta agrupadas pelo setor
     * @param minimo   mínimo de respostas por grupo exibido
     * @return grupos na ordem de exibição (setores por nome e o grupo agrupado por último);
     *         vazio quando o total não atinge o mínimo
     */
    public static <T> Map<String, List<T>> agrupar(Map<String, List<T>> porSetor, int minimo) {
        int total = porSetor.values().stream().mapToInt(List::size).sum();
        Map<String, List<T>> resultado = new LinkedHashMap<>();
        if (total == 0 || total < minimo) {
            return resultado;
        }
        Map<String, List<T>> visiveis = new TreeMap<>();
        List<T> demais = new ArrayList<>();
        porSetor.forEach((setor, itens) -> {
            if (itens.size() >= minimo) {
                visiveis.put(setor, itens);
            } else {
                demais.addAll(itens);
            }
        });
        while (!demais.isEmpty() && demais.size() < minimo && !visiveis.isEmpty()) {
            String menor = visiveis.entrySet().stream()
                    .min(Comparator.<Map.Entry<String, List<T>>>comparingInt(e -> e.getValue().size())
                            .thenComparing(Map.Entry::getKey))
                    .orElseThrow().getKey();
            demais.addAll(visiveis.remove(menor));
        }
        resultado.putAll(visiveis);
        if (!demais.isEmpty()) {
            resultado.put(GRUPO_DEMAIS, demais);
        }
        return resultado;
    }
}
