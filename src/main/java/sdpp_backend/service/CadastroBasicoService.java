package sdpp_backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.ItemCadastroRequest;
import sdpp_backend.dto.CadastroDTOs.ItemCadastroResponse;
import sdpp_backend.exception.ConflitoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.model.CargoModel;
import sdpp_backend.model.DepartamentoModel;
import sdpp_backend.repository.CargoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.DepartamentoRepository;

/** Tabelas de apoio do cadastro de colaboradores: departamentos e cargos. */
@Service
public class CadastroBasicoService {

    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;
    private final ColaboradorRepository colaboradorRepository;

    public CadastroBasicoService(DepartamentoRepository departamentoRepository, CargoRepository cargoRepository,
                                 ColaboradorRepository colaboradorRepository) {
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    // ---------- Departamentos ----------

    @Transactional(readOnly = true)
    public List<ItemCadastroResponse> listarDepartamentos() {
        return departamentoRepository.findAll().stream()
                .sorted(Comparator.comparing(DepartamentoModel::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(ItemCadastroResponse::de).toList();
    }

    @Transactional
    public ItemCadastroResponse salvarDepartamento(Long id, ItemCadastroRequest req) {
        DepartamentoModel d = id == null ? new DepartamentoModel() : departamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Departamento não encontrado."));
        String nome = req.nome().trim();
        if (!nome.equalsIgnoreCase(d.getNome()) && departamentoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um departamento com este nome.");
        }
        d.setNome(nome);
        d.setDescricao(req.descricao());
        return ItemCadastroResponse.de(departamentoRepository.save(d));
    }

    @Transactional
    public void excluirDepartamento(Long id) {
        if (!departamentoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Departamento não encontrado.");
        }
        if (colaboradorRepository.countByDepartamentoId(id) > 0) {
            throw new ConflitoException("Há colaboradores neste departamento; transfira-os antes de excluir.");
        }
        departamentoRepository.deleteById(id);
    }

    // ---------- Cargos ----------

    @Transactional(readOnly = true)
    public List<ItemCadastroResponse> listarCargos() {
        return cargoRepository.findAll().stream()
                .sorted(Comparator.comparing(CargoModel::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(ItemCadastroResponse::de).toList();
    }

    @Transactional
    public ItemCadastroResponse salvarCargo(Long id, ItemCadastroRequest req) {
        CargoModel c = id == null ? new CargoModel() : cargoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cargo não encontrado."));
        String nome = req.nome().trim();
        if (!nome.equalsIgnoreCase(c.getNome()) && cargoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um cargo com este nome.");
        }
        c.setNome(nome);
        c.setDescricao(req.descricao());
        return ItemCadastroResponse.de(cargoRepository.save(c));
    }

    @Transactional
    public void excluirCargo(Long id) {
        if (!cargoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Cargo não encontrado.");
        }
        if (colaboradorRepository.countByCargoId(id) > 0) {
            throw new ConflitoException("Há colaboradores com este cargo; altere-os antes de excluir.");
        }
        cargoRepository.deleteById(id);
    }
}
