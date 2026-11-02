package sdpp_backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.AvaliacaoDTOs.NineBoxItemResponse;
import sdpp_backend.dto.AvaliacaoDTOs.NineBoxRequest;
import sdpp_backend.dto.AvaliacaoDTOs.NineBoxResponse;
import sdpp_backend.dto.AvaliacaoDTOs.QuadranteRequest;
import sdpp_backend.dto.AvaliacaoDTOs.QuadranteResponse;
import sdpp_backend.service.NineBoxService;

@RestController
@RequestMapping("/api/nine-box")
public class NineBoxController {

    private final NineBoxService service;

    public NineBoxController(NineBoxService service) {
        this.service = service;
    }

    @GetMapping
    public NineBoxResponse matriz(@RequestParam Long cicloId, @RequestParam(required = false) Long departamentoId) {
        return service.matriz(cicloId, departamentoId);
    }

    @PutMapping
    public NineBoxItemResponse registrar(@Valid @RequestBody NineBoxRequest req) {
        return service.registrar(req);
    }

    @GetMapping("/quadrantes")
    public List<QuadranteResponse> quadrantes() {
        return service.quadrantes();
    }

    @PutMapping("/quadrantes/{id}")
    @PreAuthorize("hasRole('RH')")
    public QuadranteResponse atualizarQuadrante(@PathVariable Long id, @Valid @RequestBody QuadranteRequest req) {
        return service.atualizarQuadrante(id, req);
    }
}
