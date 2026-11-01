package sdpp_backend.repository;

import sdpp_backend.model.CompetenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetenciaRepository
        extends JpaRepository<CompetenciaModel, Long> {
}
