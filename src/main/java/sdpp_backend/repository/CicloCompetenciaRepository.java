package sdpp_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.CicloCompetenciaModel;

public interface CicloCompetenciaRepository extends JpaRepository<CicloCompetenciaModel, Long> {

    List<CicloCompetenciaModel> findByCicloIdOrderByCompetenciaTipoAscCompetenciaNomeAsc(Long cicloId);

    void deleteByCicloId(Long cicloId);

    boolean existsByCompetenciaId(Long competenciaId);
}
