package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.CicloCompetenciaModel;

public interface CicloCompetenciaRepository
        extends JpaRepository<CicloCompetenciaModel, Long> {
}
