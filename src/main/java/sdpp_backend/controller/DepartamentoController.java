
package sdpp_backend.controller;

import sdpp_backend.model.DepartamentoModel;
import sdpp_backend.service.DepartamentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    private final DepartamentoService service;

    public DepartamentoController(DepartamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<DepartamentoModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public DepartamentoModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public DepartamentoModel salvar(@RequestBody DepartamentoModel departamento) {
        return service.salvar(departamento);
    }

    @PutMapping("/{id}")
    public DepartamentoModel atualizar(
            @PathVariable Long id,
            @RequestBody DepartamentoModel departamento) {

        DepartamentoModel existente = service.buscarPorId(id);

        existente.setNome(departamento.getNome());
        existente.setDescricao(departamento.getDescricao());

        return service.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}


