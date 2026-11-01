package sdpp_backend.service;

import sdpp_backend.model.CompetenciaModel;
import sdpp_backend.repository.CompetenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetenciaService {

    private final CompetenciaRepository repository;

    public CompetenciaService(CompetenciaRepository repository) {
        this.repository = repository;
    }

    public List<CompetenciaModel> listarTodos() {
        return repository.findAll();
    }

    public CompetenciaModel buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public CompetenciaModel salvar(CompetenciaModel competencia) {
        return repository.save(competencia);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}