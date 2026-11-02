package sdpp_backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.DepartamentoModel;

public interface DepartamentoRepository extends JpaRepository<DepartamentoModel, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
