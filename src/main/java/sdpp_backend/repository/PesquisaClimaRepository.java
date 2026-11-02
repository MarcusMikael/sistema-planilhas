package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.PesquisaClimaModel;

public interface PesquisaClimaRepository
        extends JpaRepository<PesquisaClimaModel, Long> {
}