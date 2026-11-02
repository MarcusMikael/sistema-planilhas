package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.AvaliacaoModel;

public interface AvaliacaoRepository
        extends JpaRepository<AvaliacaoModel, Long> {
}