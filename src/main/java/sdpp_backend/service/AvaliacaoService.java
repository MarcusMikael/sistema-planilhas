package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.repository.AvaliacaoRepository;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository repository;

    public AvaliacaoService(AvaliacaoRepository repository) {
        this.repository = repository;
    }

    public List<AvaliacaoModel> listarTodos() {
        return repository.findAll();
    }

    public AvaliacaoModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
    }

    public AvaliacaoModel salvar(AvaliacaoModel avaliacao) {
        return repository.save(avaliacao);
    }

    public AvaliacaoModel atualizar(Long id, AvaliacaoModel avaliacao) {
        AvaliacaoModel existente = buscarPorId(id);

        existente.setColaborador(avaliacao.getColaborador());
        existente.setCiclo(avaliacao.getCiclo());
        existente.setAvaliador(avaliacao.getAvaliador());
        existente.setTipo(avaliacao.getTipo());
        existente.setStatus(avaliacao.getStatus());
        existente.setDataAvaliacao(avaliacao.getDataAvaliacao());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        AvaliacaoModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}