package sdpp_backend.repository;

import sdpp_backend.model.CicloAvaliacaoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CicloAvaliacaoRepository
        extends JpaRepository<CicloAvaliacaoModel, Long> {
}