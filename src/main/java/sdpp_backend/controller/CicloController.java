package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.AvaliacaoDTOs.CicloRequest;
import sdpp_backend.dto.AvaliacaoDTOs.CicloResponse;
import sdpp_backend.dto.AvaliacaoDTOs.MapaCompetenciasResponse;
import sdpp_backend.dto.AvaliacaoDTOs.ResultadoColaboradorResponse;
import sdpp_backend.dto.AvaliacaoDTOs.SelecaoCompetenciasRequest;
import sdpp_backend.service.CicloService;
import sdpp_backend.service.ResultadoService;

@RestController
@RequestMapping("/api/ciclos")
public class CicloController {

    private final CicloService service;
    private final ResultadoService resultadoService;

    public CicloController(CicloService service, ResultadoService resultadoService) {
        this.service = service;
        this.resultadoService = resultadoService;
    }

    @GetMapping
    public List<CicloResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CicloResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public CicloResponse salvar(@Valid @RequestBody CicloRequest req) {
        return service.salvar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public CicloResponse atualizar(@PathVariable Long id, @Valid @RequestBody CicloRequest req) {
        return service.atualizar(id, req);
    }

    @PutMapping("/{id}/competencias")
    @PreAuthorize("hasRole('RH')")
    public CicloResponse selecionarCompetencias(@PathVariable Long id,
                                                @Valid @RequestBody SelecaoCompetenciasRequest req) {
        return service.selecionarCompetencias(id, req);
    }

    @PostMapping("/{id}/liberar")
    @PreAuthorize("hasRole('RH')")
    public CicloResponse liberar(@PathVariable Long id) {
        return service.liberar(id);
    }

    @PostMapping("/{id}/liberar-resultados")
    @PreAuthorize("hasRole('RH')")
    public CicloResponse liberarResultados(@PathVariable Long id) {
        return service.liberarResultados(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/{id}/resultados/{colaboradorId}")
    public ResultadoColaboradorResponse resultado(@PathVariable Long id, @PathVariable Long colaboradorId) {
        return resultadoService.resultadoColaborador(id, colaboradorId);
    }

    @GetMapping("/{id}/mapa-competencias")
    public MapaCompetenciasResponse mapa(@PathVariable Long id) {
        return resultadoService.mapa(id);
    }
}
