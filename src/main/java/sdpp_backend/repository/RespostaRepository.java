package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.RespostaModel;

public interface RespostaRepository
        extends JpaRepository<RespostaModel, Long> {
}
