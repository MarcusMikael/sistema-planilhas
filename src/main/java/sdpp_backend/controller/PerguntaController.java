package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.PerguntaModel;
import sdpp_backend.service.PerguntaService;

@RestController
@RequestMapping("/perguntas")
public class PerguntaController {

    private final PerguntaService service;

    public PerguntaController(PerguntaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PerguntaModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public PerguntaModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public PerguntaModel salvar(@RequestBody PerguntaModel pergunta) {
        return service.salvar(pergunta);
    }

    @PutMapping("/{id}")
    public PerguntaModel atualizar(
            @PathVariable Long id,
            @RequestBody PerguntaModel pergunta) {
        return service.atualizar(id, pergunta);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
