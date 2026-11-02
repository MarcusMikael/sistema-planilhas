package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.NineBoxModel;

public interface NineBoxRepository
        extends JpaRepository<NineBoxModel, Long> {
}