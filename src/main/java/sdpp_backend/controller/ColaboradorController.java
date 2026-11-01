
package sdpp_backend.controller;

import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.service.ColaboradorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colaboradores")
public class ColaboradorController {

    private final ColaboradorService service;

    public ColaboradorController(ColaboradorService service) {
        this.service = service;
    }

    @GetMapping
    public List<ColaboradorModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ColaboradorModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ColaboradorModel salvar(@RequestBody ColaboradorModel colaborador) {
        return service.salvar(colaborador);
    }

    @PutMapping("/{id}")
public ColaboradorModel atualizar(
        @PathVariable Long id,
        @RequestBody ColaboradorModel colaborador) {

    return service.atualizar(id, colaborador);
}

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

