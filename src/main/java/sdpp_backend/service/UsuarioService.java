package sdpp_backend.service;

import sdpp_backend.model.UsuarioModel;
import sdpp_backend.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioModel> listarTodos() {
        return repository.findAll();
    }

    public UsuarioModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public UsuarioModel salvar(UsuarioModel usuario) {

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        return repository.save(usuario);
    }

    public UsuarioModel atualizar(Long id, UsuarioModel usuario) {

        UsuarioModel existente = buscarPorId(id);

        existente.setEmail(usuario.getEmail());
        existente.setAtivo(usuario.getAtivo());

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        return repository.save(existente);
    }

    public void excluir(Long id) {

        UsuarioModel usuario = buscarPorId(id);

        usuario.setAtivo(false);

        repository.save(usuario);
    }
}