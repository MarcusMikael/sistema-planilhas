package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.CadastroDTOs.ColaboradorRequest;
import sdpp_backend.dto.CadastroDTOs.ColaboradorResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.HistoricoResponse;
import sdpp_backend.service.ColaboradorService;
import sdpp_backend.service.IndicadorService;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService service;
    private final IndicadorService indicadorService;

    public ColaboradorController(ColaboradorService service, IndicadorService indicadorService) {
        this.service = service;
        this.indicadorService = indicadorService;
    }

    /** RH: todos; Gestor: ele e sua equipe; Colaborador: só ele. */
    @GetMapping
    public List<ColaboradorResponse> listar() {
        return service.listar();
    }

    @GetMapping("/equipe")
    public List<ColaboradorResponse> equipe() {
        return service.equipe();
    }

    @GetMapping("/{id}")
    public ColaboradorResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/historico")
    public HistoricoResponse historico(@PathVariable Long id) {
        return indicadorService.historico(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public ColaboradorResponse salvar(@Valid @RequestBody ColaboradorRequest req) {
        return service.salvar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public ColaboradorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ColaboradorRequest req) {
        return service.atualizar(id, req);
    }

    /** Desativa o colaborador (exclusão lógica). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void desativar(@PathVariable Long id) {
        service.desativar(id);
    }
}
