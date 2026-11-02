package sdpp_backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.CargoModel;

public interface CargoRepository extends JpaRepository<CargoModel, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
