package sdpp_backend.repository;

import java.util.Optional;

import sdpp_backend.model.enums.Nivel;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.QuadranteModel;

public interface QuadranteRepository extends JpaRepository<QuadranteModel, Long> {

    Optional<QuadranteModel> findByDesempenhoAndPotencial(Nivel desempenho, Nivel potencial);
}
