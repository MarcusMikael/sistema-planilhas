package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.DesenvolvimentoDTOs.AcaoRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.AcaoResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.PDIRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.PDIResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.ProgressoRequest;
import sdpp_backend.service.PDIService;

@RestController
@RequestMapping("/api")
public class PDIController {

    private final PDIService service;

    public PDIController(PDIService service) {
        this.service = service;
    }

    @GetMapping("/pdis")
    public List<PDIResponse> listar(@RequestParam(required = false) Long colaboradorId) {
        return service.listar(colaboradorId);
    }

    @GetMapping("/pdis/{id}")
    public PDIResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/pdis")
    @ResponseStatus(HttpStatus.CREATED)
    public PDIResponse criar(@Valid @RequestBody PDIRequest req) {
        return service.criar(req);
    }

    @PutMapping("/pdis/{id}")
    public PDIResponse atualizar(@PathVariable Long id, @Valid @RequestBody PDIRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/pdis/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PostMapping("/pdis/{id}/acoes")
    @ResponseStatus(HttpStatus.CREATED)
    public AcaoResponse adicionarAcao(@PathVariable Long id, @Valid @RequestBody AcaoRequest req) {
        return service.adicionarAcao(id, req);
    }

    @PutMapping("/acoes-pdi/{id}")
    public AcaoResponse atualizarAcao(@PathVariable Long id, @Valid @RequestBody AcaoRequest req) {
        return service.atualizarAcao(id, req);
    }

    @PatchMapping("/acoes-pdi/{id}/progresso")
    public AcaoResponse atualizarProgresso(@PathVariable Long id, @Valid @RequestBody ProgressoRequest req) {
        return service.atualizarProgresso(id, req);
    }

    @DeleteMapping("/acoes-pdi/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirAcao(@PathVariable Long id) {
        service.excluirAcao(id);
    }
}
