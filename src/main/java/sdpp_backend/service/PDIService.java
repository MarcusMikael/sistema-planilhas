package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.PDIModel;
import sdpp_backend.repository.PDIRepository;

@Service
public class PDIService {

    private final PDIRepository repository;

    public PDIService(PDIRepository repository) {
        this.repository = repository;
    }

    public List<PDIModel> listarTodos() {
        return repository.findAll();
    }

    public PDIModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PDI não encontrado"));
    }

    public PDIModel salvar(PDIModel pdi) {
        return repository.save(pdi);
    }

    public PDIModel atualizar(Long id, PDIModel pdi) {
        PDIModel existente = buscarPorId(id);

        existente.setColaborador(pdi.getColaborador());
        existente.setCiclo(pdi.getCiclo());
        existente.setObjetivo(pdi.getObjetivo());
        existente.setDataInicio(pdi.getDataInicio());
        existente.setDataFim(pdi.getDataFim());
        existente.setStatus(pdi.getStatus());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        PDIModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}