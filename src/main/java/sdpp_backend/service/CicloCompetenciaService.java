package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.CicloCompetenciaModel;
import sdpp_backend.repository.CicloCompetenciaRepository;

@Service
public class CicloCompetenciaService {

    private final CicloCompetenciaRepository repository;

    public CicloCompetenciaService(CicloCompetenciaRepository repository) {
        this.repository = repository;
    }

    public List<CicloCompetenciaModel> listarTodos() {
        return repository.findAll();
    }

    public CicloCompetenciaModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Associação não encontrada"));
    }

    public CicloCompetenciaModel salvar(CicloCompetenciaModel cicloCompetencia) {
        return repository.save(cicloCompetencia);
    }

    public CicloCompetenciaModel atualizar(Long id, CicloCompetenciaModel cicloCompetencia) {
        CicloCompetenciaModel existente = buscarPorId(id);

        existente.setCiclo(cicloCompetencia.getCiclo());
        existente.setCompetencia(cicloCompetencia.getCompetencia());
        existente.setPeso(cicloCompetencia.getPeso());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        CicloCompetenciaModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}