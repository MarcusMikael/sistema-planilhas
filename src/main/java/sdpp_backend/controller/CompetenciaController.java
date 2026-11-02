package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.CadastroDTOs.CompetenciaRequest;
import sdpp_backend.dto.CadastroDTOs.CompetenciaResponse;
import sdpp_backend.service.CompetenciaService;

@RestController
@RequestMapping("/api/competencias")
public class CompetenciaController {

    private final CompetenciaService service;

    public CompetenciaController(CompetenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompetenciaResponse> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public CompetenciaResponse salvar(@Valid @RequestBody CompetenciaRequest req) {
        return service.salvar(null, req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public CompetenciaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CompetenciaRequest req) {
        return service.salvar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
