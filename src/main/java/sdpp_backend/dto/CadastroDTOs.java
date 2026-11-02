package sdpp_backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import sdpp_backend.model.CargoModel;
import sdpp_backend.model.ColaboradorModel;
import sdpp_backend.model.CompetenciaModel;
import sdpp_backend.model.DepartamentoModel;
import sdpp_backend.model.UsuarioModel;
import sdpp_backend.model.enums.Perfil;
import sdpp_backend.model.enums.TipoCompetencia;

/** DTOs dos módulos SEG, COL e do catálogo de competências. */
public final class CadastroDTOs {

    private CadastroDTOs() {
    }

    // ---------- Autenticação ----------

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Informe a senha") String senha) {
    }

    public record LoginResponse(String token, UsuarioAtualResponse usuario) {
    }

    public record UsuarioAtualResponse(Long id, String nome, String email, Perfil perfil,
                                       Long colaboradorId, Boolean ehGestorDeEquipe) {
    }

    public record TrocarSenhaRequest(
            @NotBlank(message = "Informe a senha atual") String senhaAtual,
            @NotBlank @Size(min = 6, message = "A nova senha deve ter ao menos 6 caracteres") String novaSenha) {
    }

    // ---------- Usuários ----------

    public record UsuarioRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
            @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres") String senha,
            @NotNull(message = "Informe o perfil") Perfil perfil,
            Boolean ativo,
            Long colaboradorId) {
    }

    public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, Boolean ativo,
                                  Long colaboradorId, String colaboradorNome) {

        public static UsuarioResponse de(UsuarioModel u, ColaboradorModel c) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getAtivo(),
                    c == null ? null : c.getId(), c == null ? null : c.getNome());
        }
    }

    // ---------- Departamentos e cargos ----------

    public record ItemCadastroRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @Size(max = 255) String descricao) {
    }

    public record ItemCadastroResponse(Long id, String nome, String descricao) {

        public static ItemCadastroResponse de(DepartamentoModel d) {
            return new ItemCadastroResponse(d.getId(), d.getNome(), d.getDescricao());
        }

        public static ItemCadastroResponse de(CargoModel c) {
            return new ItemCadastroResponse(c.getId(), c.getNome(), c.getDescricao());
        }
    }

    // ---------- Colaboradores ----------

    public record ColaboradorRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
            @NotNull(message = "Informe o cargo") Long cargoId,
            @NotNull(message = "Informe o departamento") Long departamentoId,
            Long gestorId,
            @NotNull(message = "Informe a data de admissão")
            @PastOrPresent(message = "A data de admissão não pode estar no futuro") LocalDate dataAdmissao,
            Boolean ativo) {
    }

    public record ColaboradorResponse(Long id, String nome, String email, LocalDate dataAdmissao, Boolean ativo,
                                      Long cargoId, String cargoNome, Long departamentoId, String departamentoNome,
                                      Long gestorId, String gestorNome, Long usuarioId, Perfil perfil) {

        public static ColaboradorResponse de(ColaboradorModel c) {
            var g = c.getGestor();
            var u = c.getUsuario();
            return new ColaboradorResponse(c.getId(), c.getNome(), c.getEmail(), c.getDataAdmissao(), c.getAtivo(),
                    c.getCargo().getId(), c.getCargo().getNome(),
                    c.getDepartamento().getId(), c.getDepartamento().getNome(),
                    g == null ? null : g.getId(), g == null ? null : g.getNome(),
                    u == null ? null : u.getId(), u == null ? null : u.getPerfil());
        }
    }

    // ---------- Competências ----------

    public record CompetenciaRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @Size(max = 1000) String descricao,
            @NotNull(message = "Informe o tipo") TipoCompetencia tipo,
            Boolean ativo) {
    }

    public record CompetenciaResponse(Long id, String nome, String descricao, TipoCompetencia tipo, Boolean ativo) {

        public static CompetenciaResponse de(CompetenciaModel c) {
            return new CompetenciaResponse(c.getId(), c.getNome(), c.getDescricao(), c.getTipo(), c.getAtivo());
        }
    }
}
