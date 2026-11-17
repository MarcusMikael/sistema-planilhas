package sdpp_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.AvaliacaoModel;
import sdpp_backend.model.enums.TipoAvaliacao;

public interface AvaliacaoRepository extends JpaRepository<AvaliacaoModel, Long> {

    Optional<AvaliacaoModel> findByColaboradorIdAndCicloIdAndTipo(Long colaboradorId, Long cicloId, TipoAvaliacao tipo);

    List<AvaliacaoModel> findByCicloId(Long cicloId);

    List<AvaliacaoModel> findByColaboradorId(Long colaboradorId);

    boolean existsByCicloId(Long cicloId);
}
