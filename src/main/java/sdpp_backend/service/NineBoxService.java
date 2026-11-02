package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sdpp_backend.model.NineBoxModel;
import sdpp_backend.repository.NineBoxRepository;

@Service
public class NineBoxService {

    private final NineBoxRepository repository;

    public NineBoxService(NineBoxRepository repository) {
        this.repository = repository;
    }

    public List<NineBoxModel> listarTodos() {
        return repository.findAll();
    }

    public NineBoxModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro Nine Box não encontrado"));
    }

    public NineBoxModel salvar(NineBoxModel nineBox) {
        return repository.save(nineBox);
    }

    public NineBoxModel atualizar(Long id, NineBoxModel nineBox) {
        NineBoxModel existente = buscarPorId(id);

        existente.setColaborador(nineBox.getColaborador());
        existente.setCiclo(nineBox.getCiclo());
        existente.setDesempenho(nineBox.getDesempenho());
        existente.setPotencial(nineBox.getPotencial());
        existente.setFaixa(nineBox.getFaixa());

        return repository.save(existente);
    }

    public void excluir(Long id) {
        NineBoxModel existente = buscarPorId(id);
        repository.delete(existente);
    }
}