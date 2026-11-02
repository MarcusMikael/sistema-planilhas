package sdpp_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sdpp_backend.dto.DesenvolvimentoDTOs.IndicadoresResponse;
import sdpp_backend.service.IndicadorService;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final IndicadorService service;

    public IndicadorController(IndicadorService service) {
        this.service = service;
    }

    @GetMapping
    public IndicadoresResponse indicadores() {
        return service.indicadores();
    }
}
