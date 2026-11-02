package sdpp_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sdpp_backend.model.AcaoPDIModel;

public interface AcaoPDIRepository extends JpaRepository<AcaoPDIModel, Long> {

    List<AcaoPDIModel> findByPdiIdOrderByDataInicio(Long pdiId);

    List<AcaoPDIModel> findByPdiIdIn(List<Long> pdiIds);

    void deleteByPdiId(Long pdiId);
}
