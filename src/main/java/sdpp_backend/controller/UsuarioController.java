package sdpp_backend.controller;

import sdpp_backend.model.UsuarioModel;
import sdpp_backend.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public UsuarioModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public UsuarioModel salvar(@RequestBody UsuarioModel usuario) {
        return service.salvar(usuario);
    }

    @PutMapping("/{id}")
    public UsuarioModel atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioModel usuario) {

        UsuarioModel existente = service.buscarPorId(id);

        existente.setEmail(usuario.getEmail());
        existente.setSenha(usuario.getSenha());
        existente.setAtivo(usuario.getAtivo());

        return service.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
