package sdpp_backend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parâmetros configuráveis do SDPP (prefixo "sdpp" no application.properties).
 * Regras ainda pendentes de validação ficam aqui para não exigir mudança de código
 * (Arquitetura 5.4 – Variabilidade).
 */
@ConfigurationProperties(prefix = "sdpp")
public record SdppProperties(Jwt jwt, Cors cors, NineBox ninebox, Clima clima, Seed seed) {

    public record Jwt(String secret, int expiracaoHoras) {
    }

    public record Cors(List<String> origens) {
    }

    /** Faixas da média do gestor para sugerir o nível de desempenho (PA-03). */
    public record NineBox(double limiteMedio, double limiteAlto) {
    }

    /** Mínimo de respostas para exibir um resultado agregado (RN-06, PA-06). */
    public record Clima(int minimoRespostas) {
    }

    /** Carrega usuários e dados fictícios de demonstração (RES-02). */
    public record Seed(boolean demo) {
    }
}
