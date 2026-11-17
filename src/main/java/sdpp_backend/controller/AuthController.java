package sdpp_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import sdpp_backend.dto.CadastroDTOs.LoginRequest;
import sdpp_backend.dto.CadastroDTOs.LoginResponse;
import sdpp_backend.dto.CadastroDTOs.TokenResponse;
import sdpp_backend.dto.CadastroDTOs.TrocarSenhaRequest;
import sdpp_backend.dto.CadastroDTOs.UsuarioAtualResponse;
import sdpp_backend.security.CookieSessao;
import sdpp_backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final CookieSessao cookie;

    public AuthController(AuthService service, CookieSessao cookie) {
        this.service = service;
        this.cookie = cookie;
    }

    /**
     * Autentica e grava a sessão no cookie HttpOnly (usado pelo navegador).
     * O token também vem no corpo para clientes de API (testes e scripts), que usam o cabeçalho Authorization.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req, HttpServletResponse resposta) {
        LoginResponse login = service.login(req);
        cookie.gravar(resposta, login.token());
        return login;
    }

    /** Encerra a sessão do navegador apagando o cookie (o JavaScript não consegue apagar um cookie HttpOnly). */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse resposta) {
        cookie.apagar(resposta);
    }

    @GetMapping("/me")
    public UsuarioAtualResponse me() {
        return service.atual();
    }

    /** Troca a senha, renova o cookie e devolve um token novo; os tokens anteriores deixam de valer. */
    @PostMapping("/senha")
    public TokenResponse trocarSenha(@Valid @RequestBody TrocarSenhaRequest req, HttpServletResponse resposta) {
        TokenResponse novo = service.trocarSenha(req);
        cookie.gravar(resposta, novo.token());
        return novo;
    }
}
