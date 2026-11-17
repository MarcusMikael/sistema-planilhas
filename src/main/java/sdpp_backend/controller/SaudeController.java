package sdpp_backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verificação de saúde pública e leve (não consulta o banco nem expõe dados).
 * Serve para o "Health Check Path" do Render e para serviços de ping que mantêm o backend acordado.
 */
@RestController
public class SaudeController {

    @GetMapping("/api/saude")
    public Map<String, String> saude() {
        return Map.of("status", "ok");
    }
}
