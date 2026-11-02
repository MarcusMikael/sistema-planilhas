package sdpp_backend;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import sdpp_backend.config.DataInitializer;

/**
 * Testes de integração da API com os dados fictícios de demonstração.
 * Cada teste referencia o critério de aceitação do DRS que verifica (RNF-QUA-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SdppBackendApplicationTests {

    @Autowired
    private MockMvc mvc;

    // ---------- Apoio ----------

    private String token(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + DataInitializer.SENHA_DEMO + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private MockHttpServletRequestBuilder como(String email, MockHttpServletRequestBuilder req) throws Exception {
        return req.header("Authorization", "Bearer " + token(email)).contentType(MediaType.APPLICATION_JSON);
    }

    private String json(String email, MockHttpServletRequestBuilder req) throws Exception {
        return mvc.perform(como(email, req)).andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
    }

    private Long idColaborador(String nome) throws Exception {
        List<Integer> ids = JsonPath.read(json("rh@sdpp.local", get("/api/colaboradores")),
                "$[?(@.nome == \"" + nome + "\")].id");
        return ids.get(0).longValue();
    }

    private Long idCiclo(String nome) throws Exception {
        List<Integer> ids = JsonPath.read(json("rh@sdpp.local", get("/api/ciclos")), "$[?(@.nome == \"" + nome + "\")].id");
        return ids.get(0).longValue();
    }

    // ---------- SEG ----------

    @Test
    @DisplayName("CA-RF-SEG-01: credenciais válidas autenticam com perfil; inválidas são negadas")
    void login() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gestor@sdpp.local\",\"senha\":\"" + DataInitializer.SENHA_DEMO + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.perfil").value("GESTOR"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gestor@sdpp.local\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/colaboradores")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/colaboradores").header("Authorization", "Bearer token.invalido.x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RNF-SEG-02: a senha nunca é devolvida pela API")
    void senhaNaoExposta() throws Exception {
        String body = json("rh@sdpp.local", get("/api/usuarios"));
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("senha").doesNotContain("$2a$");
    }

    @Test
    @DisplayName("CA-RF-SEG-02: colaborador A não acessa dados de B; gestor só vê a própria equipe")
    void permissoesPorPessoaEEquipe() throws Exception {
        Long pedro = idColaborador("Pedro Santos");
        Long marina = idColaborador("Marina Oliveira");
        mvc.perform(como("marina@sdpp.local", get("/api/colaboradores/" + pedro))).andExpect(status().isForbidden());
        mvc.perform(como("marina@sdpp.local", get("/api/pdis?colaboradorId=" + idColaborador("Joana D'Ávila"))))
                .andExpect(status().isForbidden());
        mvc.perform(como("gestora.comercial@sdpp.local", get("/api/colaboradores/" + marina)))
                .andExpect(status().isForbidden());
        mvc.perform(como("gestor@sdpp.local", get("/api/colaboradores/" + marina))).andExpect(status().isOk());
        mvc.perform(como("marina@sdpp.local", get("/api/colaboradores")))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(como("marina@sdpp.local", get("/api/usuarios"))).andExpect(status().isForbidden());
    }

    // ---------- COL ----------

    @Test
    @DisplayName("CA-RF-COL-01/02: RH cadastra e altera colaborador; outros perfis não")
    void cadastroDeColaborador() throws Exception {
        String depts = json("rh@sdpp.local", get("/api/departamentos"));
        String cargos = json("rh@sdpp.local", get("/api/cargos"));
        Integer dept = JsonPath.read(depts, "$[0].id");
        Integer dept2 = JsonPath.read(depts, "$[1].id");
        Integer cargo = JsonPath.read(cargos, "$[0].id");
        String corpo = "{\"nome\":\"Teste Novo\",\"email\":\"teste.novo@sdpp.local\",\"cargoId\":" + cargo
                + ",\"departamentoId\":" + dept + ",\"gestorId\":" + idColaborador("Fernanda Costa")
                + ",\"dataAdmissao\":\"2024-01-10\"}";
        mvc.perform(como("gestor@sdpp.local", post("/api/colaboradores").content(corpo)))
                .andExpect(status().isForbidden());
        String criado = json("rh@sdpp.local", post("/api/colaboradores").content(corpo));
        Integer id = JsonPath.read(criado, "$.id");
        mvc.perform(como("rh@sdpp.local", post("/api/colaboradores").content(corpo))).andExpect(status().isConflict());
        mvc.perform(como("rh@sdpp.local", put("/api/colaboradores/" + id).content(corpo.replace(
                        "\"departamentoId\":" + dept, "\"departamentoId\":" + dept2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departamentoId").value(dept2));
        mvc.perform(como("rh@sdpp.local", post("/api/colaboradores").content("{\"nome\":\"\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    // ---------- CIC ----------

    @Test
    @DisplayName("CA-RF-CIC-02/03: no máximo 5 de cada tipo; só libera com 5 + 5")
    void composicaoDoCiclo() throws Exception {
        String ciclo = json("rh@sdpp.local", post("/api/ciclos")
                .content("{\"nome\":\"Ciclo Teste\",\"dataInicio\":\"2020-01-01\",\"dataFim\":\"2020-02-01\"}"));
        Integer id = JsonPath.read(ciclo, "$.id");
        String catalogo = json("rh@sdpp.local", get("/api/competencias"));
        List<Integer> comp = JsonPath.read(catalogo, "$[?(@.tipo == 'COMPORTAMENTAL')].id");
        List<Integer> tec = JsonPath.read(catalogo, "$[?(@.tipo == 'TECNICA')].id");

        List<Integer> seis = new ArrayList<>(comp.subList(0, 6));
        mvc.perform(como("rh@sdpp.local", put("/api/ciclos/" + id + "/competencias").content(ids(seis))))
                .andExpect(status().isUnprocessableContent());

        List<Integer> cincoQuatro = new ArrayList<>(comp.subList(0, 5));
        cincoQuatro.addAll(tec.subList(0, 4));
        json("rh@sdpp.local", put("/api/ciclos/" + id + "/competencias").content(ids(cincoQuatro)));
        mvc.perform(como("rh@sdpp.local", post("/api/ciclos/" + id + "/liberar")))
                .andExpect(status().isUnprocessableContent());

        cincoQuatro.add(tec.get(4));
        json("rh@sdpp.local", put("/api/ciclos/" + id + "/competencias").content(ids(cincoQuatro)));
        mvc.perform(como("rh@sdpp.local", post("/api/ciclos/" + id + "/liberar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIBERADO"));

        mvc.perform(como("rh@sdpp.local", post("/api/ciclos")
                        .content("{\"nome\":\"X\",\"dataInicio\":\"2020-02-01\",\"dataFim\":\"2020-01-01\"}")))
                .andExpect(status().isUnprocessableContent());
    }

    private static String ids(List<Integer> ids) {
        return "{\"competenciaIds\":[" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]}";
    }

    // ---------- AVA ----------

    @Test
    @DisplayName("CA-RF-AVA-01/03 e CA-RN-02: nota 6 é rejeitada; não conclui com nota ausente")
    void autoavaliacao() throws Exception {
        Long ciclo = idCiclo("Ciclo 2026/1");
        Long lucas = idColaborador("Lucas Rocha");
        String av = json("lucas@sdpp.local", post("/api/avaliacoes")
                .content("{\"cicloId\":" + ciclo + ",\"colaboradorId\":" + lucas + ",\"tipo\":\"AUTOAVALIACAO\"}"));
        Integer id = JsonPath.read(av, "$.id");
        List<Integer> ccs = JsonPath.read(av, "$.itens[*].cicloCompetenciaId");
        org.assertj.core.api.Assertions.assertThat(ccs).hasSize(10);

        mvc.perform(como("lucas@sdpp.local", put("/api/avaliacoes/" + id + "/notas")
                        .content("{\"notas\":[{\"cicloCompetenciaId\":" + ccs.get(0) + ",\"nota\":6}]}")))
                .andExpect(status().isBadRequest());

        String noveNotas = ccs.subList(0, 9).stream().map(cc -> "{\"cicloCompetenciaId\":" + cc + ",\"nota\":4}")
                .collect(Collectors.joining(","));
        json("lucas@sdpp.local", put("/api/avaliacoes/" + id + "/notas").content("{\"notas\":[" + noveNotas + "]}"));
        mvc.perform(como("lucas@sdpp.local", post("/api/avaliacoes/" + id + "/concluir")))
                .andExpect(status().isUnprocessableContent());

        json("lucas@sdpp.local", put("/api/avaliacoes/" + id + "/notas")
                .content("{\"notas\":[{\"cicloCompetenciaId\":" + ccs.get(9) + ",\"nota\":0}]}"));
        mvc.perform(como("lucas@sdpp.local", post("/api/avaliacoes/" + id + "/concluir")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.dataAvaliacao").exists());

        // A autoavaliação de outra pessoa não pode ser aberta
        mvc.perform(como("lucas@sdpp.local", post("/api/avaliacoes").content("{\"cicloId\":" + ciclo
                        + ",\"colaboradorId\":" + idColaborador("Pedro Santos") + ",\"tipo\":\"AUTOAVALIACAO\"}")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CA-RF-AVA-02: gestor avalia a sua equipe, de forma independente; não avalia outra equipe")
    void avaliacaoDoGestor() throws Exception {
        Long ciclo = idCiclo("Ciclo 2026/1");
        Long beatriz = idColaborador("Beatriz Alves");
        String corpo = "{\"cicloId\":" + ciclo + ",\"colaboradorId\":" + beatriz + ",\"tipo\":\"GESTOR\"}";
        mvc.perform(como("gestor@sdpp.local", post("/api/avaliacoes").content(corpo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avaliadorNome").value("Carlos Lima"))
                .andExpect(jsonPath("$.tipo").value("GESTOR"));
        mvc.perform(como("gestora.comercial@sdpp.local", post("/api/avaliacoes").content(corpo)))
                .andExpect(status().isForbidden());
        mvc.perform(como("beatriz@sdpp.local", post("/api/avaliacoes").content(corpo.replace("GESTOR", "AUTOAVALIACAO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("AUTOAVALIACAO"));
    }

    @Test
    @DisplayName("CA-RF-AVA-05: resultados não liberados não ficam disponíveis ao colaborador")
    void liberacaoDeResultados() throws Exception {
        Long atual = idCiclo("Ciclo 2026/1");
        Long marina = idColaborador("Marina Oliveira");
        mvc.perform(como("marina@sdpp.local", get("/api/ciclos/" + atual + "/resultados/" + marina)))
                .andExpect(status().isForbidden());
        mvc.perform(como("rh@sdpp.local", get("/api/ciclos/" + atual + "/resultados/" + marina)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CA-RF-AVA-06/08a/08b: comparação gestor 4,1 × autoavaliação 3,9")
    void comparacaoDeResultados() throws Exception {
        Long anterior = idCiclo("Ciclo 2025/2");
        Long marina = idColaborador("Marina Oliveira");
        for (String quem : List.of("marina@sdpp.local", "gestor@sdpp.local")) {
            mvc.perform(como(quem, get("/api/ciclos/" + anterior + "/resultados/" + marina)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.gestor.comportamental").value(4.0))
                    .andExpect(jsonPath("$.gestor.tecnica").value(4.2))
                    .andExpect(jsonPath("$.gestor.geral").value(4.1))
                    .andExpect(jsonPath("$.autoavaliacao.tecnica").value(3.8))
                    .andExpect(jsonPath("$.autoavaliacao.geral").value(3.9));
        }
        mvc.perform(como("pedro@sdpp.local", get("/api/ciclos/" + anterior + "/resultados/" + marina)))
                .andExpect(status().isForbidden());
        mvc.perform(como("rh@sdpp.local", get("/api/ciclos/" + anterior + "/mapa-competencias")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comportamentais.length()").value(5))
                .andExpect(jsonPath("$.tecnicas.length()").value(5));
    }

    // ---------- NBX ----------

    @Test
    @DisplayName("CA-RF-NBX-02a/02c/03: exemplos de [P2] nos quadrantes; sem dados fica pendente")
    void nineBox() throws Exception {
        Long anterior = idCiclo("Ciclo 2025/2");
        String matriz = json("gestor@sdpp.local", get("/api/nine-box?cicloId=" + anterior));
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<String>>read(matriz, "$.itens[?(@.colaboradorNome == 'Marina Oliveira')].quadrante.rotulo"))
                .containsExactly("Mantenedor");
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<String>>read(matriz, "$.itens[?(@.colaboradorNome == 'Pedro Santos')].quadrante.rotulo"))
                .containsExactly("Forte desempenho");
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<String>>read(matriz, "$.itens[?(@.colaboradorNome == \"Joana D'Ávila\")].quadrante.rotulo"))
                .containsExactly("Enigma");
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<Boolean>>read(matriz, "$.itens[?(@.colaboradorNome == 'Beatriz Alves')].pendente"))
                .containsExactly(true);
        mvc.perform(como("marina@sdpp.local", get("/api/nine-box?cicloId=" + anterior)))
                .andExpect(status().isForbidden());

        // Só potencial informado → continua pendente; Alto × Alto → Alto potencial
        Long lucas = idColaborador("Lucas Rocha");
        mvc.perform(como("gestor@sdpp.local", put("/api/nine-box").content(
                        "{\"cicloId\":" + anterior + ",\"colaboradorId\":" + lucas + ",\"potencial\":\"ALTO\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendente").value(true));
        mvc.perform(como("gestor@sdpp.local", put("/api/nine-box").content("{\"cicloId\":" + anterior
                        + ",\"colaboradorId\":" + lucas + ",\"desempenho\":\"ALTO\",\"potencial\":\"ALTO\"}")))
                .andExpect(jsonPath("$.quadrante.rotulo").value("Alto potencial"));
        mvc.perform(como("gestor@sdpp.local", put("/api/nine-box").content("{\"cicloId\":" + anterior
                        + ",\"colaboradorId\":" + lucas + ",\"desempenho\":\"MEDIO\",\"potencial\":\"ALTO\"}")))
                .andExpect(jsonPath("$.quadrante.rotulo").value("Em crescimento"));
        mvc.perform(como("gestor@sdpp.local", put("/api/nine-box").content("{\"cicloId\":" + anterior
                        + ",\"colaboradorId\":" + lucas + ",\"desempenho\":\"OTIMO\"}")))
                .andExpect(status().isBadRequest());
    }

    // ---------- PDI ----------

    @Test
    @DisplayName("CA-RF-PDI-01/02/03: PDI sem cargo desejado; término antes do início é rejeitado; dono atualiza")
    void pdi() throws Exception {
        Long pedro = idColaborador("Pedro Santos");
        String pdi = json("gestor@sdpp.local", post("/api/pdis").content("{\"colaboradorId\":" + pedro
                + ",\"objetivo\":\"Preparar para liderança técnica\",\"habilidadesDesenvolver\":\"Liderança\","
                + "\"habilidadesSucesso\":\"Análise de dados\",\"anotacoes\":\"Sem cargo desejado\"}"));
        Integer id = JsonPath.read(pdi, "$.id");
        mvc.perform(como("gestor@sdpp.local", post("/api/pdis/" + id + "/acoes").content(
                        "{\"descricao\":\"Curso\",\"dataInicio\":\"2026-03-10\",\"dataTermino\":\"2026-03-05\"}")))
                .andExpect(status().isUnprocessableContent());
        String acao = json("gestor@sdpp.local", post("/api/pdis/" + id + "/acoes").content(
                "{\"descricao\":\"Curso\",\"dataInicio\":\"2026-03-10\",\"dataTermino\":\"2026-03-10\","
                        + "\"entrega\":\"Certificado\",\"recursos\":\"R$ 100,00\",\"status\":\"A_FAZER\"}"));
        Integer acaoId = JsonPath.read(acao, "$.id");

        String progresso = "{\"status\":\"EM_ANDAMENTO\",\"progresso\":50}";
        mvc.perform(como("marina@sdpp.local", patch("/api/acoes-pdi/" + acaoId + "/progresso").content(progresso)))
                .andExpect(status().isForbidden());
        mvc.perform(como("pedro@sdpp.local", patch("/api/acoes-pdi/" + acaoId + "/progresso").content(progresso)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progresso").value(50));
        mvc.perform(como("pedro@sdpp.local", post("/api/pdis/" + id + "/acoes").content(
                        "{\"descricao\":\"X\",\"dataInicio\":\"2026-03-10\",\"dataTermino\":\"2026-03-11\"}")))
                .andExpect(status().isForbidden());
        mvc.perform(como("gestora.comercial@sdpp.local", get("/api/pdis/" + id))).andExpect(status().isForbidden());
    }

    // ---------- CLI ----------

    @Test
    @DisplayName("CA-RF-CLI-02/03 e CA-RF-IND-03: uma resposta por campanha, escala 1–5, só agregado")
    void pesquisaDeClima() throws Exception {
        String lista = json("gestor@sdpp.local", get("/api/pesquisas-clima"));
        Integer id = JsonPath.read(lista, "$[0].id");
        String detalhe = json("gestor@sdpp.local", get("/api/pesquisas-clima/" + id));
        List<Integer> perguntas = JsonPath.read(detalhe, "$.perguntas[?(@.tipo == 'ESCALA')].id");
        org.assertj.core.api.Assertions.assertThat(JsonPath.<List<?>>read(detalhe, "$.perguntas")).hasSize(20);

        String invalida = "{\"respostas\":[" + perguntas.stream().map(p -> "{\"perguntaId\":" + p + ",\"valor\":6}")
                .collect(Collectors.joining(",")) + "]}";
        mvc.perform(como("gestor@sdpp.local", post("/api/pesquisas-clima/" + id + "/respostas").content(invalida)))
                .andExpect(status().isUnprocessableContent());

        String valida = invalida.replace("\"valor\":6", "\"valor\":4");
        mvc.perform(como("gestor@sdpp.local", post("/api/pesquisas-clima/" + id + "/respostas").content(valida)))
                .andExpect(status().isNoContent());
        mvc.perform(como("gestor@sdpp.local", post("/api/pesquisas-clima/" + id + "/respostas").content(valida)))
                .andExpect(status().isConflict());

        mvc.perform(como("rh@sdpp.local", get("/api/respostas"))).andExpect(status().isNotFound());
        mvc.perform(como("gestor@sdpp.local", get("/api/pesquisas-clima/" + id + "/resultados")))
                .andExpect(status().isForbidden());
        String resultados = json("rh@sdpp.local", get("/api/pesquisas-clima/" + id + "/resultados"));
        org.assertj.core.api.Assertions.assertThat(resultados)
                .doesNotContain("colaborador").doesNotContain("gestor@sdpp.local");
        mvc.perform(como("rh@sdpp.local", get("/api/pesquisas-clima/" + id + "/resultados")))
                .andExpect(jsonPath("$.totalParticipacoes").value(6))
                .andExpect(jsonPath("$.perguntas[1].exibido").value(true))
                .andExpect(jsonPath("$.perguntas[1].porSetor[*].setor", hasItem("Tecnologia")))
                .andExpect(jsonPath("$.perguntas[1].porSetor[*].setor", not(hasItem("Comercial"))));
    }

    // ---------- IND ----------

    @Test
    @DisplayName("CA-RF-IND-01/02: indicadores e histórico respeitam o escopo do perfil")
    void indicadoresEHistorico() throws Exception {
        mvc.perform(como("rh@sdpp.local", get("/api/indicadores")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.escopo").value("ORGANIZACAO"));
        mvc.perform(como("gestor@sdpp.local", get("/api/indicadores")))
                .andExpect(jsonPath("$.escopo").value("EQUIPE"))
                .andExpect(jsonPath("$.totalColaboradores").value(5));
        Long joana = idColaborador("Joana D'Ávila");
        mvc.perform(como("joana@sdpp.local", get("/api/colaboradores/" + joana + "/historico")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pdis.length()").value(1))
                .andExpect(jsonPath("$.ciclos[1].cicloNome").value("Ciclo 2025/2"))
                .andExpect(jsonPath("$.ciclos[1].gestor.geral").exists())
                .andExpect(jsonPath("$.ciclos[1].quadrante").doesNotExist());
        mvc.perform(como("gestor@sdpp.local", get("/api/colaboradores/" + joana + "/historico")))
                .andExpect(jsonPath("$.ciclos[1].quadrante").value("Enigma"));
        mvc.perform(como("marina@sdpp.local", get("/api/colaboradores/" + joana + "/historico")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário não exclui a si mesmo; exclusão de usuário é lógica")
    void usuarios() throws Exception {
        String lista = json("rh@sdpp.local", get("/api/usuarios"));
        List<Integer> ana = JsonPath.read(lista, "$[?(@.email == 'rh@sdpp.local')].id");
        mvc.perform(como("rh@sdpp.local", delete("/api/usuarios/" + ana.get(0))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Gestor com perfil RH avalia a equipe direta (gestores que respondem ao RH)")
    void rhAvaliaSuaEquipe() throws Exception {
        Long ciclo = idCiclo("Ciclo 2026/1");
        Long carlos = idColaborador("Carlos Lima");
        String pend = json("rh@sdpp.local", get("/api/avaliacoes/pendentes"));
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<String>>read(pend, "$[?(@.tipo == 'GESTOR')].colaboradorNome"))
                .contains("Carlos Lima", "Fernanda Costa");
        mvc.perform(como("rh@sdpp.local", post("/api/avaliacoes").content(
                        "{\"cicloId\":" + ciclo + ",\"colaboradorId\":" + carlos + ",\"tipo\":\"GESTOR\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avaliadorNome").value("Ana Souza"));
        // Gestor não avalia o próprio gestor
        mvc.perform(como("gestor@sdpp.local", post("/api/avaliacoes").content("{\"cicloId\":" + ciclo
                        + ",\"colaboradorId\":" + idColaborador("Ana Souza") + ",\"tipo\":\"GESTOR\"}")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Colaborador desativado perde o acesso")
    void desativacaoBloqueiaAcesso() throws Exception {
        String depts = json("rh@sdpp.local", get("/api/departamentos"));
        String cargos = json("rh@sdpp.local", get("/api/cargos"));
        String corpo = "{\"nome\":\"Temporario\",\"email\":\"temp@sdpp.local\",\"cargoId\":"
                + JsonPath.read(cargos, "$[0].id") + ",\"departamentoId\":" + JsonPath.read(depts, "$[0].id")
                + ",\"dataAdmissao\":\"2025-05-05\"}";
        Integer id = JsonPath.read(json("rh@sdpp.local", post("/api/colaboradores").content(corpo)), "$.id");
        json("rh@sdpp.local", post("/api/usuarios").content("{\"nome\":\"Temporario\",\"email\":\"temp@sdpp.local\","
                + "\"senha\":\"" + DataInitializer.SENHA_DEMO + "\",\"perfil\":\"COLABORADOR\",\"colaboradorId\":" + id + "}"));
        String tokenTemp = token("temp@sdpp.local");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokenTemp))
                .andExpect(jsonPath("$.colaboradorId").value(id));

        json("rh@sdpp.local", put("/api/colaboradores/" + id).content(corpo.replace("}", ",\"ativo\":false}")));
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokenTemp))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"temp@sdpp.local\",\"senha\":\"" + DataInitializer.SENHA_DEMO + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rotas do RH respondem 403 a outros perfis mesmo com corpo inválido")
    void permissaoAntesDaValidacao() throws Exception {
        for (String rota : List.of("/api/colaboradores", "/api/ciclos", "/api/competencias", "/api/pesquisas-clima")) {
            mvc.perform(como("gestor@sdpp.local", post(rota).content("{}"))).andExpect(status().isForbidden());
        }
        mvc.perform(como("gestor@sdpp.local", post("/api/pesquisas-clima/1/respostas").content("{}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Token adulterado é rejeitado")
    void tokenAdulterado() throws Exception {
        String t = token("marina@sdpp.local");
        String adulterado = t.substring(0, t.length() - 2) + (t.endsWith("A") ? "BB" : "AA");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA-RF-CIC-04: catálogo inicial 10 + 10 e descrição editável pelo RH")
    void catalogoDeCompetencias() throws Exception {
        String catalogo = json("marina@sdpp.local", get("/api/competencias"));
        org.assertj.core.api.Assertions.assertThat(JsonPath.<List<?>>read(catalogo, "$[?(@.tipo == 'COMPORTAMENTAL')]")).hasSize(10);
        org.assertj.core.api.Assertions.assertThat(JsonPath.<List<?>>read(catalogo, "$[?(@.tipo == 'TECNICA')]")).hasSize(10);
        Integer id = JsonPath.<List<Integer>>read(catalogo, "$[?(@.nome == 'Organização')].id").get(0);
        String corpo = "{\"nome\":\"Organização\",\"descricao\":\"Nova descrição\",\"tipo\":\"COMPORTAMENTAL\"}";
        mvc.perform(como("marina@sdpp.local", put("/api/competencias/" + id).content(corpo)))
                .andExpect(status().isForbidden());
        mvc.perform(como("rh@sdpp.local", put("/api/competencias/" + id).content(corpo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Nova descrição"));
    }

    @Test
    @DisplayName("CA-RF-NBX-04: quadrante exibe descrição e as quatro ações sugeridas")
    void planoDeAcaoDoQuadrante() throws Exception {
        String q = json("gestor@sdpp.local", get("/api/nine-box/quadrantes"));
        org.assertj.core.api.Assertions.assertThat(JsonPath.<List<?>>read(q, "$")).hasSize(9);
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<String>>read(q, "$[?(@.rotulo == 'Enigma')].descricao"))
                .containsExactly("Possui grande capacidade, mas não está entregando.");
        org.assertj.core.api.Assertions.assertThat(
                JsonPath.<List<List<String>>>read(q, "$[?(@.rotulo == 'Enigma')].acoes").get(0)).hasSize(4);
    }

    @Test
    @DisplayName("CA-RF-IND-01: colaborador vê apenas indicadores pessoais")
    void indicadoresDoColaborador() throws Exception {
        mvc.perform(como("pedro@sdpp.local", get("/api/indicadores")))
                .andExpect(jsonPath("$.escopo").value("PESSOAL"))
                .andExpect(jsonPath("$.totalColaboradores").value(1))
                .andExpect(jsonPath("$.pesquisaAtual").doesNotExist());
    }
}
