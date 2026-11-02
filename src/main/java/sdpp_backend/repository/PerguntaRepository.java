package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.PerguntaModel;

public interface PerguntaRepository
        extends JpaRepository<PerguntaModel, Long> {
}
