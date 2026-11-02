package sdpp_backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.ParticipacaoClimaModel;

public interface ParticipacaoClimaRepository extends JpaRepository<ParticipacaoClimaModel, Long> {

    boolean existsByPesquisaIdAndColaboradorId(Long pesquisaId, Long colaboradorId);

    long countByPesquisaId(Long pesquisaId);

    boolean existsByColaboradorId(Long colaboradorId);
}
