
package sdpp_backend.service;

import sdpp_backend.model.DepartamentoModel;
import sdpp_backend.repository.DepartamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {

    private final DepartamentoRepository repository;

    public DepartamentoService(DepartamentoRepository repository) {
        this.repository = repository;
    }

    public List<DepartamentoModel> listarTodos() {
        return repository.findAll();
    }

    public DepartamentoModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Departamento não encontrado"));
    }

    public DepartamentoModel salvar(DepartamentoModel departamento) {
        return repository.save(departamento);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Departamento não encontrado");
        }

        repository.deleteById(id);
    }
}

