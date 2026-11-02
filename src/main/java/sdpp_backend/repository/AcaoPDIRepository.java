package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.AcaoPDIModel;

public interface AcaoPDIRepository
        extends JpaRepository<AcaoPDIModel, Long> {
}