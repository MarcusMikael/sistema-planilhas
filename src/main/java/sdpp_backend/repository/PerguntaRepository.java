package sdpp_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.PerguntaModel;

public interface PerguntaRepository extends JpaRepository<PerguntaModel, Long> {

    List<PerguntaModel> findByPesquisaIdOrderByOrdem(Long pesquisaId);

    long countByPesquisaId(Long pesquisaId);
}
