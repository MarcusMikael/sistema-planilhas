package sdpp_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.PontuacaoModel;

public interface PontuacaoRepository extends JpaRepository<PontuacaoModel, Long> {

    List<PontuacaoModel> findByAvaliacaoIdOrderByCicloCompetenciaCompetenciaTipoAscCicloCompetenciaCompetenciaNomeAsc(Long avaliacaoId);

    List<PontuacaoModel> findByAvaliacaoIdIn(List<Long> avaliacaoIds);

    void deleteByAvaliacaoId(Long avaliacaoId);
}
