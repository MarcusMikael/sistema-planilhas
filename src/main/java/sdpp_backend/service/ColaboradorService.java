package sdpp_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.dto.CadastroDTOs.ColaboradorRequest;
import sdpp_backend.dto.CadastroDTOs.ColaboradorResponse;
import sdpp_backend.exception.ConflitoException;
import sdpp_backend.exception.RecursoNaoEncontradoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.repository.CargoRepository;
import sdpp_backend.repository.ColaboradorRepository;
import sdpp_backend.repository.DepartamentoRepository;
import sdpp_backend.security.AcessoService;

/** Cadastro de colaboradores (RF-COL-01, RF-COL-02). Escrita só pelo RH; leitura conforme a equipe. */
@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final AcessoService acesso;

    public ColaboradorService(ColaboradorRepository repository, CargoRepository cargoRepository,
                              DepartamentoRepository departamentoRepository, AcessoService acesso) {
        this.repository = repository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return acesso.colaboradoresVisiveis(acesso.atual()).stream().map(ColaboradorResponse::de).toList();
    }

    /** Lista simplificada para escolher o gestor responsável no formulário do RH. */
    @Transactional(readOnly = true)
    public List<ColaboradorResponse> equipe() {
        return acesso.equipe(acesso.atual()).stream().map(ColaboradorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse buscarPorId(Long id) {
        ColaboradorModel c = buscar(id);
        acesso.exigirPodeVer(acesso.atual(), c);
        return ColaboradorResponse.de(c);
    }

    @Transactional
    public ColaboradorResponse salvar(ColaboradorRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um colaborador com este e-mail.");
        }
        ColaboradorModel c = new ColaboradorModel();
        preencher(c, req, email);
        return ColaboradorResponse.de(repository.save(c));
    }

    @Transactional
    public ColaboradorResponse atualizar(Long id, ColaboradorRequest req) {
        ColaboradorModel c = buscar(id);
        String email = req.email().trim().toLowerCase();
        if (!c.getEmail().equalsIgnoreCase(email) && repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um colaborador com este e-mail.");
        }
        preencher(c, req, email);
        return ColaboradorResponse.de(c);
    }

    /**
     * Exclusão lógica: o colaborador é desativado para preservar avaliações, PDIs e histórico.
     * A conta de acesso vinculada também é desativada.
     */
    @Transactional
    public void desativar(Long id) {
        ColaboradorModel c = buscar(id);
        c.setAtivo(false);
        if (c.getUsuario() != null) {
            c.getUsuario().setAtivo(false);
        }
    }

    private void preencher(ColaboradorModel c, ColaboradorRequest req, String email) {
        c.setNome(req.nome().trim());
        c.setEmail(email);
        c.setDataAdmissao(req.dataAdmissao());
        c.setAtivo(req.ativo() == null || req.ativo());
        c.setCargo(cargoRepository.findById(req.cargoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cargo não encontrado.")));
        c.setDepartamento(departamentoRepository.findById(req.departamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Departamento não encontrado.")));
        c.setGestor(req.gestorId() == null ? null : validarGestor(c, req.gestorId()));
        // Colaborador inativo não acessa o sistema
        if (!c.getAtivo() && c.getUsuario() != null) {
            c.getUsuario().setAtivo(false);
        }
    }

    /** O gestor não pode ser o próprio colaborador nem gerar ciclo na hierarquia. */
    private ColaboradorModel validarGestor(ColaboradorModel c, Long gestorId) {
        ColaboradorModel gestor = repository.findById(gestorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gestor não encontrado."));
        boolean gestorAlterado = c.getGestor() == null || !c.getGestor().getId().equals(gestorId);
        if (gestorAlterado && !Boolean.TRUE.equals(gestor.getAtivo())) {
            throw new RegraNegocioException("O gestor responsável precisa estar ativo.");
        }
        for (ColaboradorModel g = gestor; g != null; g = g.getGestor()) {
            if (c.getId() != null && c.getId().equals(g.getId())) {
                throw new RegraNegocioException("Gestor inválido: criaria um ciclo na hierarquia.");
            }
        }
        return gestor;
    }

    private ColaboradorModel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado."));
    }
}
