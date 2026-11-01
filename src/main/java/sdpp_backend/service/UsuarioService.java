package sdpp_backend.service;

import sdpp_backend.model.UsuarioModel;
import sdpp_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<UsuarioModel> listarTodos() {
        return repository.findAll();
    }

    public UsuarioModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public UsuarioModel salvar(UsuarioModel usuario) {
        return repository.save(usuario);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }

        repository.deleteById(id);
    }
}