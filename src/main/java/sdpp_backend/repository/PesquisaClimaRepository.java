package sdpp_backend.repository;

import java.util.List;

import sdpp_backend.model.enums.StatusPesquisa;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.PesquisaClimaModel;

public interface PesquisaClimaRepository extends JpaRepository<PesquisaClimaModel, Long> {

    List<PesquisaClimaModel> findAllByOrderByDataInicioDesc();

    List<PesquisaClimaModel> findByStatusOrderByDataInicioDesc(StatusPesquisa status);
}
