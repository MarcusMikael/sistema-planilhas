package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.PDIModel;

public interface PDIRepository extends JpaRepository<PDIModel, Long> {
}
