package sdpp_backend.controller;

import sdpp_backend.model.CompetenciaModel;
import sdpp_backend.service.CompetenciaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competencias")
public class CompetenciaController {

    private final CompetenciaService service;

    public CompetenciaController(CompetenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompetenciaModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public CompetenciaModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public CompetenciaModel salvar(@RequestBody CompetenciaModel competencia) {
        return service.salvar(competencia);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}