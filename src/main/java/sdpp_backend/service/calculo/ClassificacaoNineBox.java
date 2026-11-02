package sdpp_backend.service.calculo;

import sdpp_backend.model.enums.Nivel;

/** Regras puras da Matriz Nine Box (RN-03, RN-12, PA-03). */
public final class ClassificacaoNineBox {

    private ClassificacaoNineBox() {
    }

    /**
     * Sugere o nível de desempenho a partir da média do gestor (0 a 5).
     * A sugestão é só apoio: o gestor confirma ou altera (RN-04).
     */
    public static Nivel sugerirDesempenho(Double mediaGestor, double limiteMedio, double limiteAlto) {
        if (mediaGestor == null) {
            return null;
        }
        if (mediaGestor < limiteMedio) {
            return Nivel.BAIXO;
        }
        return mediaGestor < limiteAlto ? Nivel.MEDIO : Nivel.ALTO;
    }

    /** Sem dado em um dos eixos, a classificação permanece pendente (RF-NBX-02). */
    public static boolean pendente(Nivel desempenho, Nivel potencial) {
        return desempenho == null || potencial == null;
    }
}
