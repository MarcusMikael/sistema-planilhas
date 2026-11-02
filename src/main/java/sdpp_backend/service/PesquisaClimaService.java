package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.PesquisaClimaModel;
import sdpp_backend.repository.PesquisaClimaRepository;

@Service
public class PesquisaClimaService {

    private final PesquisaClimaRepository repository;

    public PesquisaClimaService(PesquisaClimaRepository repository) {
        this.repository = repository;
    }

    public List<PesquisaClimaModel> listarTodos() {
        return repository.findAll();
    }

    public PesquisaClimaModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pesquisa não encontrada"));
    }

    public PesquisaClimaModel salvar(PesquisaClimaModel pesquisa) {
        return repository.save(pesquisa);
    }

    public PesquisaClimaModel atualizar(Long id, PesquisaClimaModel pesquisa) {
        PesquisaClimaModel existente = buscarPorId(id);

        existente.setTitulo(pesquisa.getTitulo());
        existente.setDataInicio(pesquisa.getDataInicio());
        existente.setDataFim(pesquisa.getDataFim());
        existente.setStatus(pesquisa.getStatus());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        PesquisaClimaModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}