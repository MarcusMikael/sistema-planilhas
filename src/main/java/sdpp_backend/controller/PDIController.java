package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.PDIModel;
import sdpp_backend.service.PDIService;

@RestController
@RequestMapping("/pdis")
public class PDIController {

    private final PDIService service;

    public PDIController(PDIService service) {
        this.service = service;
    }

    @GetMapping
    public List<PDIModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public PDIModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public PDIModel salvar(@RequestBody PDIModel pdi) {
        return service.salvar(pdi);
    }

    @PutMapping("/{id}")
    public PDIModel atualizar(
            @PathVariable Long id,
            @RequestBody PDIModel pdi) {
        return service.atualizar(id, pdi);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}