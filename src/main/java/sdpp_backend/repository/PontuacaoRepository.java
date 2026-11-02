package sdpp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sdpp_backend.model.PontuacaoModel;

public interface PontuacaoRepository
        extends JpaRepository<PontuacaoModel, Long> {
}