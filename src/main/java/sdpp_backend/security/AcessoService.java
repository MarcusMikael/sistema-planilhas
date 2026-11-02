package sdpp_backend.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import sdpp_backend.exception.AcessoNegadoException;
import sdpp_backend.exception.RegraNegocioException;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.repository.ColaboradorRepository;

/**
 * Regras de acesso por perfil, pessoa e equipe (RF-SEG-02, RNF-SEG-04, CA-RF-SEG-02):
 * RH acessa todos; Gestor acessa a si e à sua equipe direta; Colaborador acessa só a si.
 */
@Service
public class AcessoService {

    private final ColaboradorRepository colaboradorRepository;

    public AcessoService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    public UsuarioLogado atual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioLogado u)) {
            throw new AcessoNegadoException("Autenticação necessária.");
        }
        return u;
    }

    /** Id do colaborador vinculado ao usuário atual; exigido para avaliar, responder clima etc. */
    public Long colaboradorIdObrigatorio(UsuarioLogado u) {
        if (u.colaboradorId() == null) {
            throw new RegraNegocioException("Sua conta não está vinculada a um colaborador.");
        }
        return u.colaboradorId();
    }

    /**
     * Gestor responsável pelo cadastro (PRE-03). Vale para perfis Gestor e RH: um usuário de RH que gerencia
     * pessoas (ex.: os gestores da área) também avalia e acompanha a sua equipe direta.
     */
    public boolean ehGestorDe(UsuarioLogado u, ColaboradorModel c) {
        return u.podeGerirEquipe() && u.colaboradorId() != null && c.getGestor() != null
                && u.colaboradorId().equals(c.getGestor().getId());
    }

    public boolean ehOProprio(UsuarioLogado u, ColaboradorModel c) {
        return u.colaboradorId() != null && u.colaboradorId().equals(c.getId());
    }

    public boolean podeVer(UsuarioLogado u, ColaboradorModel c) {
        return u.isRH() || ehOProprio(u, c) || ehGestorDe(u, c);
    }

    public void exigirPodeVer(UsuarioLogado u, ColaboradorModel c) {
        if (!podeVer(u, c)) {
            throw new AcessoNegadoException("Você não tem acesso aos dados deste colaborador.");
        }
    }

    public void exigirRHOuGestorDe(UsuarioLogado u, ColaboradorModel c) {
        if (!u.isRH() && !ehGestorDe(u, c)) {
            throw new AcessoNegadoException("Apenas o RH ou o gestor responsável podem realizar esta operação.");
        }
    }

    /** Colaboradores visíveis ao usuário: todos (RH), o próprio e a equipe (Gestor), só o próprio (Colaborador). */
    public List<ColaboradorModel> colaboradoresVisiveis(UsuarioLogado u) {
        if (u.isRH()) {
            return colaboradorRepository.findAllByOrderByNome();
        }
        List<ColaboradorModel> lista = new ArrayList<>();
        if (u.colaboradorId() != null) {
            colaboradorRepository.findById(u.colaboradorId()).ifPresent(lista::add);
            if (u.isGestor()) {
                lista.addAll(colaboradorRepository.findByGestorIdOrderByNome(u.colaboradorId()));
            }
        }
        return lista;
    }

    /** Equipe direta do gestor atual (vazia para quem não tem colaborador vinculado). */
    public List<ColaboradorModel> equipe(UsuarioLogado u) {
        if (u.colaboradorId() == null) {
            return List.of();
        }
        return colaboradorRepository.findByGestorIdAndAtivoTrueOrderByNome(u.colaboradorId());
    }
}
