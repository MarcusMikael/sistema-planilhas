package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.service.CicloAvaliacaoService;

@RestController
@RequestMapping("/ciclos")
public class CicloAvaliacaoController {

    private final CicloAvaliacaoService service;

    public CicloAvaliacaoController(CicloAvaliacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CicloAvaliacaoModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public CicloAvaliacaoModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public CicloAvaliacaoModel salvar(@RequestBody CicloAvaliacaoModel ciclo) {
        return service.salvar(ciclo);
    }

    @PutMapping("/{id}")
    public CicloAvaliacaoModel atualizar(
            @PathVariable Long id,
            @RequestBody CicloAvaliacaoModel ciclo) {
        return service.atualizar(id, ciclo);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}