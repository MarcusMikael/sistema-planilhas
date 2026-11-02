package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.AvaliacaoDTOs.AbrirAvaliacaoRequest;
import sdpp_backend.dto.AvaliacaoDTOs.AvaliacaoResponse;
import sdpp_backend.dto.AvaliacaoDTOs.NotasRequest;
import sdpp_backend.dto.AvaliacaoDTOs.PendenciaResponse;
import sdpp_backend.service.AvaliacaoService;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<AvaliacaoResponse> listar(@RequestParam Long cicloId) {
        return service.listar(cicloId);
    }

    @GetMapping("/pendentes")
    public List<PendenciaResponse> pendentes() {
        return service.pendentes();
    }

    @GetMapping("/{id}")
    public AvaliacaoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    /** Abre a autoavaliação ou a avaliação do gestor; se já existir, devolve a existente. */
    @PostMapping
    public AvaliacaoResponse abrir(@Valid @RequestBody AbrirAvaliacaoRequest req) {
        return service.abrir(req);
    }

    @PutMapping("/{id}/notas")
    public AvaliacaoResponse salvarNotas(@PathVariable Long id, @Valid @RequestBody NotasRequest req) {
        return service.salvarNotas(id, req);
    }

    @PostMapping("/{id}/concluir")
    public AvaliacaoResponse concluir(@PathVariable Long id) {
        return service.concluir(id);
    }
}
