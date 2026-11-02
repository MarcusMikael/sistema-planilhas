package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.PontuacaoModel;
import sdpp_backend.repository.PontuacaoRepository;

@Service
public class PontuacaoService {

    private final PontuacaoRepository repository;

    public PontuacaoService(PontuacaoRepository repository) {
        this.repository = repository;
    }

    public List<PontuacaoModel> listarTodos() {
        return repository.findAll();
    }

    public PontuacaoModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pontuação não encontrada"));
    }

    public PontuacaoModel salvar(PontuacaoModel pontuacao) {
        return repository.save(pontuacao);
    }

    public PontuacaoModel atualizar(Long id, PontuacaoModel pontuacao) {
        PontuacaoModel existente = buscarPorId(id);

        existente.setAvaliacao(pontuacao.getAvaliacao());
        existente.setCicloCompetencia(pontuacao.getCicloCompetencia());
        existente.setNota(pontuacao.getNota());
        existente.setComentario(pontuacao.getComentario());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        PontuacaoModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}