package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.AcaoPDIModel;
import sdpp_backend.repository.AcaoPDIRepository;

@Service
public class AcaoPDIService {

    private final AcaoPDIRepository repository;

    public AcaoPDIService(AcaoPDIRepository repository) {
        this.repository = repository;
    }

    public List<AcaoPDIModel> listarTodos() {
        return repository.findAll();
    }

    public AcaoPDIModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ação do PDI não encontrada"));
    }

    public AcaoPDIModel salvar(AcaoPDIModel acao) {
        return repository.save(acao);
    }

    public AcaoPDIModel atualizar(Long id, AcaoPDIModel acao) {
        AcaoPDIModel existente = buscarPorId(id);

        existente.setPdi(acao.getPdi());
        existente.setDescricao(acao.getDescricao());
        existente.setResponsavel(acao.getResponsavel());
        existente.setPrazo(acao.getPrazo());
        existente.setStatus(acao.getStatus());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        AcaoPDIModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}