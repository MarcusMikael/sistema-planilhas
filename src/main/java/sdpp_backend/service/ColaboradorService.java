
package sdpp_backend.service;

import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.repository.CargoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.DepartamentoRepository;
import sdpp_backend.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public ColaboradorService(
            ColaboradorRepository repository,
            CargoRepository cargoRepository,
            DepartamentoRepository departamentoRepository,
            UsuarioRepository usuarioRepository) {

        this.repository = repository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ColaboradorModel> listarTodos() {
        return repository.findAll();
    }

    public ColaboradorModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Colaborador não encontrado"));
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

        // Busca o usuário, caso tenha sido informado
        if (colaborador.getUsuario() != null) {

            Long usuarioId = colaborador.getUsuario().getId();

            var usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (!usuario.getAtivo()) {
                throw new RegraNegocioException(
                        "Não é permitido vincular um usuário inativo");
            }

            colaborador.setUsuario(usuario);
        }

        // Associa os registros encontrados
        colaborador.setCargo(cargo);
        colaborador.setDepartamento(departamento);

        // Salva o colaborador
        return repository.save(colaborador);
    }

    public ColaboradorModel atualizar(Long id, ColaboradorModel colaborador) {

        ColaboradorModel existente = buscarPorId(id);

        existente.setNome(colaborador.getNome());
        existente.setEmail(colaborador.getEmail());
        existente.setDataAdmissao(colaborador.getDataAdmissao());
        existente.setAtivo(colaborador.getAtivo());

        // Atualiza o cargo
        if (colaborador.getCargo() != null) {
            var cargo = cargoRepository.findById(colaborador.getCargo().getId())
                    .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));

            existente.setCargo(cargo);
        }

        // Atualiza o departamento
        if (colaborador.getDepartamento() != null) {
            var departamento = departamentoRepository.findById(colaborador.getDepartamento().getId())
                    .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

            existente.setDepartamento(departamento);
        }

        // Atualiza o usuário
        if (colaborador.getUsuario() != null) {
            var usuario = usuarioRepository.findById(colaborador.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (!usuario.getAtivo()) {
                throw new RegraNegocioException(
                        "Não é permitido vincular um usuário inativo");
            }

            existente.setUsuario(usuario);
        }

        return repository.save(existente);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Colaborador não encontrado");
        }

        repository.deleteById(id);
    }
}
