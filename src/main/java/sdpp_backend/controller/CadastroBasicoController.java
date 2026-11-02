package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.CadastroDTOs.ItemCadastroRequest;
import sdpp_backend.dto.CadastroDTOs.ItemCadastroResponse;
import sdpp_backend.service.CadastroBasicoService;

/** Departamentos e cargos: leitura para usuários autenticados, escrita só pelo RH. */
@RestController
@RequestMapping("/api")
public class CadastroBasicoController {

    private final CadastroBasicoService service;

    public CadastroBasicoController(CadastroBasicoService service) {
        this.service = service;
    }

    @GetMapping("/departamentos")
    public List<ItemCadastroResponse> listarDepartamentos() {
        return service.listarDepartamentos();
    }

    @PostMapping("/departamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public ItemCadastroResponse criarDepartamento(@Valid @RequestBody ItemCadastroRequest req) {
        return service.salvarDepartamento(null, req);
    }

    @PutMapping("/departamentos/{id}")
    @PreAuthorize("hasRole('RH')")
    public ItemCadastroResponse atualizarDepartamento(@PathVariable Long id, @Valid @RequestBody ItemCadastroRequest req) {
        return service.salvarDepartamento(id, req);
    }

    @DeleteMapping("/departamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void excluirDepartamento(@PathVariable Long id) {
        service.excluirDepartamento(id);
    }

    @GetMapping("/cargos")
    public List<ItemCadastroResponse> listarCargos() {
        return service.listarCargos();
    }

    @PostMapping("/cargos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public ItemCadastroResponse criarCargo(@Valid @RequestBody ItemCadastroRequest req) {
        return service.salvarCargo(null, req);
    }

    @PutMapping("/cargos/{id}")
    @PreAuthorize("hasRole('RH')")
    public ItemCadastroResponse atualizarCargo(@PathVariable Long id, @Valid @RequestBody ItemCadastroRequest req) {
        return service.salvarCargo(id, req);
    }

    @DeleteMapping("/cargos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void excluirCargo(@PathVariable Long id) {
        service.excluirCargo(id);
    }
}
