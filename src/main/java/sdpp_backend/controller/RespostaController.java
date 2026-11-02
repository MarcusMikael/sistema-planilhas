package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.RespostaModel;
import sdpp_backend.service.RespostaService;

@RestController
@RequestMapping("/respostas")
public class RespostaController {

    private final RespostaService service;

    public RespostaController(RespostaService service) {
        this.service = service;
    }

    @GetMapping
    public List<RespostaModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public RespostaModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public RespostaModel salvar(@RequestBody RespostaModel resposta) {
        return service.salvar(resposta);
    }

    @PutMapping("/{id}")
    public RespostaModel atualizar(
            @PathVariable Long id,
            @RequestBody RespostaModel resposta) {
        return service.atualizar(id, resposta);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
