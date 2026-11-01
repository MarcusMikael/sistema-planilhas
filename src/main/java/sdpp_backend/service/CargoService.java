
package sdpp_backend.service;

import sdpp_backend.model.CargoModel;
import sdpp_backend.repository.CargoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CargoService {

    private final CargoRepository repository;

    public CargoService(CargoRepository repository) {
        this.repository = repository;
    }

    public List<CargoModel> listarTodos() {
        return repository.findAll();
    }

    public CargoModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cargo não encontrado"));
    }

    public CargoModel salvar(CargoModel cargo) {
        return repository.save(cargo);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Cargo não encontrado");
        }

        repository.deleteById(id);
    }
}

