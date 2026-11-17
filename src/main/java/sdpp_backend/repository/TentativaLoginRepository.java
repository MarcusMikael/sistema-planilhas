package sdpp_backend.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import sdpp_backend.model.TentativaLoginModel;

public interface TentativaLoginRepository extends JpaRepository<TentativaLoginModel, String> {

    @Modifying
    @Query("delete from TentativaLoginModel t where t.inicio < :limite")
    int apagarAnterioresA(Instant limite);
}
