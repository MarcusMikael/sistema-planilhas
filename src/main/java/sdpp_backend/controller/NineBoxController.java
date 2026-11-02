package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.NineBoxModel;
import sdpp_backend.service.NineBoxService;

@RestController
@RequestMapping("/nine-box")
public class NineBoxController {

    private final NineBoxService service;

    public NineBoxController(NineBoxService service) {
        this.service = service;
    }

    @GetMapping
    public List<NineBoxModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public NineBoxModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public NineBoxModel salvar(@RequestBody NineBoxModel nineBox) {
        return service.salvar(nineBox);
    }

    @PutMapping("/{id}")
    public NineBoxModel atualizar(
            @PathVariable Long id,
            @RequestBody NineBoxModel nineBox) {
        return service.atualizar(id, nineBox);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}