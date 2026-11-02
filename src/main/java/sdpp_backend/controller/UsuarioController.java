package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.CadastroDTOs.UsuarioRequest;
import sdpp_backend.dto.CadastroDTOs.UsuarioResponse;
import sdpp_backend.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('RH')")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse salvar(@Valid @RequestBody UsuarioRequest req) {
        return service.salvar(req);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return service.atualizar(id, req);
    }

    /** Desativa a conta (exclusão lógica). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        service.desativar(id);
    }
}
