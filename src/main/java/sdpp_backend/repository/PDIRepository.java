package sdpp_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.PDIModel;

public interface PDIRepository extends JpaRepository<PDIModel, Long> {

    List<PDIModel> findAllByOrderByCriadoEmDesc();

    List<PDIModel> findByColaboradorIdOrderByCriadoEmDesc(Long colaboradorId);

    List<PDIModel> findByColaboradorIdInOrderByCriadoEmDesc(List<Long> colaboradorIds);

    boolean existsByColaboradorId(Long colaboradorId);
}
