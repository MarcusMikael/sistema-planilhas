package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.CompetenciaRequest;
import sdpp_backend.dto.CadastroDTOs.CompetenciaResponse;
import sdpp_backend.exception.ConflitoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.model.CompetenciaModel;
import sdpp_backend.repository.CicloCompetenciaRepository;
import sdpp_backend.repository.CompetenciaRepository;

/** Catálogo de competências editável pelo RH (RF-CIC-04). */
@Service
public class CompetenciaService {

    private final CompetenciaRepository repository;
    private final CicloCompetenciaRepository cicloCompetenciaRepository;

    public CompetenciaService(CompetenciaRepository repository, CicloCompetenciaRepository cicloCompetenciaRepository) {
        this.repository = repository;
        this.cicloCompetenciaRepository = cicloCompetenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<CompetenciaResponse> listarTodos() {
        return repository.findAllByOrderByTipoAscNomeAsc().stream().map(CompetenciaResponse::de).toList();
    }

    @Transactional
    public CompetenciaResponse salvar(Long id, CompetenciaRequest req) {
        CompetenciaModel c = id == null ? new CompetenciaModel() : buscar(id);
        String nome = req.nome().trim();
        if (!nome.equalsIgnoreCase(c.getNome()) && repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma competência com este nome.");
        }
        c.setNome(nome);
        c.setDescricao(req.descricao());
        c.setTipo(req.tipo());
        c.setAtivo(req.ativo() == null || req.ativo());
        return CompetenciaResponse.de(repository.save(c));
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        if (cicloCompetenciaRepository.existsByCompetenciaId(id)) {
            throw new ConflitoException("A competência já foi usada em ciclos; desative-a em vez de excluir.");
        }
        repository.deleteById(id);
    }

    private CompetenciaModel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Competência não encontrada."));
    }
}
