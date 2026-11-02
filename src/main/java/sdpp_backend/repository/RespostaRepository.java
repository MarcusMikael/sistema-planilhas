package sdpp_backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.RespostaModel;

public interface RespostaRepository extends JpaRepository<RespostaModel, UUID> {

    List<RespostaModel> findByPerguntaPesquisaId(Long pesquisaId);

    boolean existsByPerguntaPesquisaId(Long pesquisaId);
}
