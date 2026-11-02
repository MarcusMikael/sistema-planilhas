package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.AcaoPDIModel;
import sdpp_backend.service.AcaoPDIService;

@RestController
@RequestMapping("/acoes-pdi")
public class AcaoPDIController {

    private final AcaoPDIService service;

    public AcaoPDIController(AcaoPDIService service) {
        this.service = service;
    }

    @GetMapping
    public List<AcaoPDIModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public AcaoPDIModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public AcaoPDIModel salvar(@RequestBody AcaoPDIModel acao) {
        return service.salvar(acao);
    }

    @PutMapping("/{id}")
    public AcaoPDIModel atualizar(
            @PathVariable Long id,
            @RequestBody AcaoPDIModel acao) {
        return service.atualizar(id, acao);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}