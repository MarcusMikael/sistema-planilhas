package sdpp_backend.repository;

import java.util.List;

import sdpp_backend.model.enums.StatusCiclo;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.CicloAvaliacaoModel;

public interface CicloAvaliacaoRepository extends JpaRepository<CicloAvaliacaoModel, Long> {

    List<CicloAvaliacaoModel> findAllByOrderByDataInicioDesc();

    List<CicloAvaliacaoModel> findByStatusInOrderByDataInicioDesc(List<StatusCiclo> status);
}
