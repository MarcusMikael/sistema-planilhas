package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.PontuacaoModel;
import sdpp_backend.service.PontuacaoService;

@RestController
@RequestMapping("/pontuacoes")
public class PontuacaoController {

    private final PontuacaoService service;

    public PontuacaoController(PontuacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontuacaoModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public PontuacaoModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public PontuacaoModel salvar(@RequestBody PontuacaoModel pontuacao) {
        return service.salvar(pontuacao);
    }

    @PutMapping("/{id}")
    public PontuacaoModel atualizar(
            @PathVariable Long id,
            @RequestBody PontuacaoModel pontuacao) {
        return service.atualizar(id, pontuacao);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}