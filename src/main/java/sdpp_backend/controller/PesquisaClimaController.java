package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.PesquisaClimaModel;
import sdpp_backend.service.PesquisaClimaService;

@RestController
@RequestMapping("/pesquisas-clima")
public class PesquisaClimaController {

    private final PesquisaClimaService service;

    public PesquisaClimaController(PesquisaClimaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PesquisaClimaModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public PesquisaClimaModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public PesquisaClimaModel salvar(@RequestBody PesquisaClimaModel pesquisa) {
        return service.salvar(pesquisa);
    }

    @PutMapping("/{id}")
    public PesquisaClimaModel atualizar(
            @PathVariable Long id,
            @RequestBody PesquisaClimaModel pesquisa) {
        return service.atualizar(id, pesquisa);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
