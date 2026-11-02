package sdpp_backend.repository;

import java.util.List;
import java.util.Optional;

import sdpp_backend.model.enums.StatusAvaliacao;
import sdpp_backend.model.enums.TipoAvaliacao;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.AvaliacaoModel;

public interface AvaliacaoRepository extends JpaRepository<AvaliacaoModel, Long> {

    Optional<AvaliacaoModel> findByColaboradorIdAndCicloIdAndTipo(Long colaboradorId, Long cicloId, TipoAvaliacao tipo);

    List<AvaliacaoModel> findByCicloId(Long cicloId);

    List<AvaliacaoModel> findByCicloIdAndTipoAndStatus(Long cicloId, TipoAvaliacao tipo, StatusAvaliacao status);

    List<AvaliacaoModel> findByColaboradorId(Long colaboradorId);

    List<AvaliacaoModel> findByAvaliadorId(Long avaliadorId);

    boolean existsByCicloId(Long cicloId);

    boolean existsByColaboradorIdOrAvaliadorId(Long colaboradorId, Long avaliadorId);
}
