package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.CicloAvaliacaoModel;
import sdpp_backend.repository.CicloAvaliacaoRepository;

@Service
public class CicloAvaliacaoService {

    private final CicloAvaliacaoRepository repository;

    public CicloAvaliacaoService(CicloAvaliacaoRepository repository) {
        this.repository = repository;
    }

    public List<CicloAvaliacaoModel> listarTodos() {
        return repository.findAll();
    }

    public CicloAvaliacaoModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ciclo não encontrado"));
    }

    public CicloAvaliacaoModel salvar(CicloAvaliacaoModel ciclo) {
        return repository.save(ciclo);
    }

    public CicloAvaliacaoModel atualizar(Long id, CicloAvaliacaoModel ciclo) {
        CicloAvaliacaoModel existente = buscarPorId(id);

        existente.setNome(ciclo.getNome());
        existente.setDataInicio(ciclo.getDataInicio());
        existente.setDataFim(ciclo.getDataFim());
        existente.setStatus(ciclo.getStatus());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        CicloAvaliacaoModel ciclo = buscarPorId(id);
        repository.delete(ciclo);
    }
}
