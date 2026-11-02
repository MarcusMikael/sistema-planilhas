package sdpp_backend.security;

import sdpp_backend.model.enums.Perfil;

/**
 * Usuário autenticado na requisição atual.
 * colaboradorId é nulo quando a conta não está vinculada a um colaborador.
 */
public record UsuarioLogado(Long usuarioId, String nome, String email, Perfil perfil, Long colaboradorId) {

    public boolean isRH() {
        return perfil == Perfil.RH;
    }

    public boolean isGestor() {
        return perfil == Perfil.GESTOR;
    }

    /** Perfis que podem avaliar e acompanhar a equipe direta definida no cadastro. */
    public boolean podeGerirEquipe() {
        return perfil == Perfil.GESTOR || perfil == Perfil.RH;
    }
}
