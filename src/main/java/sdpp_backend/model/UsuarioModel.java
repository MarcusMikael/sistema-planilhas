package sdpp_backend.model;

import jakarta.persistence.*;
import sdpp_backend.model.enums.Perfil;

@Entity
@Table(name = "usuario")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private Boolean ativo = true;

    /**
     * Versão dos tokens emitidos para a conta. Ao trocar a senha a versão aumenta e os tokens
     * anteriores deixam de valer. Coluna anulável para que o "ddl-auto=update" funcione em bancos existentes.
     */
    @Column(name = "versao_token")
    private Integer versaoToken = 0;

    public UsuarioModel() {
    }

    public int getVersaoToken() {
        return versaoToken == null ? 0 : versaoToken;
    }

    /** Invalida todos os tokens já emitidos para esta conta. */
    public void revogarTokens() {
        this.versaoToken = getVersaoToken() + 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
