package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.PerguntaModel;
import sdpp_backend.repository.PerguntaRepository;

@Service
public class PerguntaService {

    private final PerguntaRepository repository;

    public PerguntaService(PerguntaRepository repository) {
        this.repository = repository;
    }

    public List<PerguntaModel> listarTodos() {
        return repository.findAll();
    }

    public PerguntaModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));
    }

    public PerguntaModel salvar(PerguntaModel pergunta) {
        return repository.save(pergunta);
    }

    public PerguntaModel atualizar(Long id, PerguntaModel pergunta) {
        PerguntaModel existente = buscarPorId(id);

        existente.setPesquisa(pergunta.getPesquisa());
        existente.setTexto(pergunta.getTexto());
        existente.setTipo(pergunta.getTipo());
        existente.setObrigatoria(pergunta.getObrigatoria());
        existente.setOrdem(pergunta.getOrdem());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        PerguntaModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}