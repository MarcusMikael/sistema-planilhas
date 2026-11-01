
package sdpp_backend.service;

import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.repository.CargoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.DepartamentoRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;

    public ColaboradorService(
            ColaboradorRepository repository,
            CargoRepository cargoRepository,
            DepartamentoRepository departamentoRepository) {

        this.repository = repository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
    }

    public List<ColaboradorModel> listarTodos() {
        return repository.findAll();
    }

    public ColaboradorModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
    }

    public ColaboradorModel salvar(ColaboradorModel colaborador) {

        // Busca o cargo
        Long cargoId = colaborador.getCargo().getId();

        var cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));

        // Busca o departamento
        Long departamentoId = colaborador.getDepartamento().getId();

        var departamento = departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

        // Associa os registros encontrados
        colaborador.setCargo(cargo);
        colaborador.setDepartamento(departamento);

        // Salva o colaborador
        return repository.save(colaborador);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Colaborador não encontrado");
        }

        repository.deleteById(id);
    }
}
