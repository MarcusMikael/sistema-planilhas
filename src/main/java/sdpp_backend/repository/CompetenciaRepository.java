package sdpp_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.CompetenciaModel;

public interface CompetenciaRepository extends JpaRepository<CompetenciaModel, Long> {

    List<CompetenciaModel> findAllByOrderByTipoAscNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
