package sdpp_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.NineBoxModel;

public interface NineBoxRepository extends JpaRepository<NineBoxModel, Long> {

    Optional<NineBoxModel> findByColaboradorIdAndCicloId(Long colaboradorId, Long cicloId);

    List<NineBoxModel> findByCicloId(Long cicloId);

    List<NineBoxModel> findByColaboradorId(Long colaboradorId);

    boolean existsByColaboradorId(Long colaboradorId);
}
