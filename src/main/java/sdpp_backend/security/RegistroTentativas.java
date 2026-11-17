package sdpp_backend.security;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.model.TentativaLoginModel;
import sdpp_backend.repository.TentativaLoginRepository;

/**
 * Armazenamento dos contadores de falhas de login.
 * Cada operação roda em uma transação própria: o login com senha errada termina em exceção, e a falha
 * registrada não pode ser desfeita junto com a transação do login.
 */
public interface RegistroTentativas {

    record Registro(int falhas, Instant inicio) {
    }

    Optional<Registro> buscar(String chave);

    void gravar(String chave, Registro registro);

    void apagar(String chave);

    void apagarAnterioresA(Instant limite);

    @Component
    class NoBanco implements RegistroTentativas {

        private final TentativaLoginRepository repository;

        public NoBanco(TentativaLoginRepository repository) {
            this.repository = repository;
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
        public Optional<Registro> buscar(String chave) {
            return repository.findById(chave).map(t -> new Registro(t.getFalhas(), t.getInicio()));
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void gravar(String chave, Registro registro) {
            repository.save(new TentativaLoginModel(chave, registro.falhas(), registro.inicio()));
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void apagar(String chave) {
            repository.deleteById(chave);
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void apagarAnterioresA(Instant limite) {
            repository.apagarAnterioresA(limite);
        }
    }
}
