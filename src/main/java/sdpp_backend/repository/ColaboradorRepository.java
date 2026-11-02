package sdpp_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.ColaboradorModel;

public interface ColaboradorRepository extends JpaRepository<ColaboradorModel, Long> {

    Optional<ColaboradorModel> findByUsuarioId(Long usuarioId);

    List<ColaboradorModel> findByGestorIdOrderByNome(Long gestorId);

    List<ColaboradorModel> findByGestorIdAndAtivoTrueOrderByNome(Long gestorId);

    List<ColaboradorModel> findByAtivoTrueOrderByNome();

    List<ColaboradorModel> findAllByOrderByNome();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByGestorId(Long gestorId);

    long countByDepartamentoId(Long departamentoId);

    long countByCargoId(Long cargoId);
}
