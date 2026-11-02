package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.service.AvaliacaoService;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<AvaliacaoModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public AvaliacaoModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public AvaliacaoModel salvar(@RequestBody AvaliacaoModel avaliacao) {
        return service.salvar(avaliacao);
    }

    @PutMapping("/{id}")
    public AvaliacaoModel atualizar(
            @PathVariable Long id,
            @RequestBody AvaliacaoModel avaliacao) {
        return service.atualizar(id, avaliacao);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}