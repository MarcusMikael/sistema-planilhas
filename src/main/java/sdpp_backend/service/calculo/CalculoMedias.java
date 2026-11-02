package sdpp_backend.service.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;

/**
 * Médias das avaliações (RN-11): média aritmética das notas válidas.
 * Notas ausentes (null) são ignoradas e não contam como zero (RN-02), ao contrário de [P1].
 */
public final class CalculoMedias {

    private CalculoMedias() {
    }

    /** Média das notas não nulas, com duas casas decimais; null quando não há nenhuma nota válida. */
    public static Double media(Collection<Integer> notas) {
        long quantidade = notas.stream().filter(Objects::nonNull).count();
        if (quantidade == 0) {
            return null;
        }
        int soma = notas.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        return arredondar((double) soma / quantidade);
    }

    public static Double arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
