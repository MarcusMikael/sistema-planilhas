package sdpp_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sdpp_backend.model.CicloCompetenciaModel;
import sdpp_backend.service.CicloCompetenciaService;

@RestController
@RequestMapping("/ciclo-competencias")
public class CicloCompetenciaController {

    private final CicloCompetenciaService service;

    public CicloCompetenciaController(CicloCompetenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CicloCompetenciaModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public CicloCompetenciaModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public CicloCompetenciaModel salvar(@RequestBody CicloCompetenciaModel cicloCompetencia) {
        return service.salvar(cicloCompetencia);
    }

    @PutMapping("/{id}")
    public CicloCompetenciaModel atualizar(
            @PathVariable Long id,
            @RequestBody CicloCompetenciaModel cicloCompetencia) {
        return service.atualizar(id, cicloCompetencia);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
