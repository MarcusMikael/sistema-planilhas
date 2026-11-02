package sdpp_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.UsuarioModel;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
