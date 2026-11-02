package sdpp_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sdpp_backend.dto.DesenvolvimentoDTOs.PerguntaRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.PerguntaResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.PesquisaRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.PesquisaResponse;
import sdpp_backend.dto.DesenvolvimentoDTOs.RespostasRequest;
import sdpp_backend.dto.DesenvolvimentoDTOs.ResultadoClimaResponse;
import sdpp_backend.service.PesquisaClimaService;

/** Não existe rota de leitura de respostas individuais (RF-CLI-03). */
@RestController
@RequestMapping("/api/pesquisas-clima")
public class PesquisaClimaController {

    private final PesquisaClimaService service;

    public PesquisaClimaController(PesquisaClimaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PesquisaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PesquisaResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public PesquisaResponse criar(@Valid @RequestBody PesquisaRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public PesquisaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PesquisaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize("hasRole('RH')")
    public PesquisaResponse publicar(@PathVariable Long id) {
        return service.publicar(id);
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('RH')")
    public PesquisaResponse encerrar(@PathVariable Long id) {
        return service.encerrar(id);
    }

    @PostMapping("/{id}/perguntas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    public PerguntaResponse adicionarPergunta(@PathVariable Long id, @Valid @RequestBody PerguntaRequest req) {
        return service.adicionarPergunta(id, req);
    }

    @PutMapping("/perguntas/{perguntaId}")
    @PreAuthorize("hasRole('RH')")
    public PerguntaResponse atualizarPergunta(@PathVariable Long perguntaId, @Valid @RequestBody PerguntaRequest req) {
        return service.atualizarPergunta(perguntaId, req);
    }

    @DeleteMapping("/perguntas/{perguntaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RH')")
    public void excluirPergunta(@PathVariable Long perguntaId) {
        service.excluirPergunta(perguntaId);
    }

    @PostMapping("/{id}/respostas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void responder(@PathVariable Long id, @Valid @RequestBody RespostasRequest req) {
        service.responder(id, req);
    }

    @GetMapping("/{id}/resultados")
    @PreAuthorize("hasRole('RH')")
    public ResultadoClimaResponse resultados(@PathVariable Long id) {
        return service.resultados(id);
    }
}
