
package sdpp_backend.service;

import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;

    public ColaboradorService(ColaboradorRepository repository) {
        this.repository = repository;
    }

    public List<ColaboradorModel> listarTodos() {
        return repository.findAll();
    }

    public ColaboradorModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Colaborador não encontrado"));
    }

    public ColaboradorModel salvar(ColaboradorModel colaborador) {
        return repository.save(colaborador);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Colaborador não encontrado");
        }

        repository.deleteById(id);
    }
}

