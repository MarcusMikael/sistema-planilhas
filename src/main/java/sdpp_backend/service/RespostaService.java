package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.RespostaModel;
import sdpp_backend.repository.RespostaRepository;

@Service
public class RespostaService {

    private final RespostaRepository repository;

    public RespostaService(RespostaRepository repository) {
        this.repository = repository;
    }

    public List<RespostaModel> listarTodos() {
        return repository.findAll();
    }

    public RespostaModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resposta não encontrada"));
    }

    public RespostaModel salvar(RespostaModel resposta) {
        return repository.save(resposta);
    }

    public RespostaModel atualizar(Long id, RespostaModel resposta) {
        RespostaModel existente = buscarPorId(id);

        existente.setPergunta(resposta.getPergunta());
        existente.setCodigoSessao(resposta.getCodigoSessao());
        existente.setValor(resposta.getValor());
        existente.setRespondidaEm(resposta.getRespondidaEm());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        RespostaModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}
