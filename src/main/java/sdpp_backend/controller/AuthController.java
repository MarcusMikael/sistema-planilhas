package sdpp_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sdpp_backend.dto.CadastroDTOs.LoginRequest;
import sdpp_backend.dto.CadastroDTOs.LoginResponse;
import sdpp_backend.dto.CadastroDTOs.TrocarSenhaRequest;
import sdpp_backend.dto.CadastroDTOs.UsuarioAtualResponse;
import sdpp_backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @GetMapping("/me")
    public UsuarioAtualResponse me() {
        return service.atual();
    }

    @PostMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trocarSenha(@Valid @RequestBody TrocarSenhaRequest req) {
        service.trocarSenha(req);
    }
}
