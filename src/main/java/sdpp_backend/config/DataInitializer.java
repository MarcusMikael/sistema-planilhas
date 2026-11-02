package sdpp_backend.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sdpp_backend.model.*;
import sdpp_backend.model.enums.*;
import sdpp_backend.repository.*;
import sdpp_backend.service.QuestionarioPadrao;

/**
 * Carga inicial.
 * Sempre: catálogo de competências de [P1] (RF-CIC-04, Anexo A.2) e quadrantes da Nine Box de [P2]
 * (RN-12, Anexo A.3). Com sdpp.seed.demo=true: dados FICTÍCIOS de demonstração (RES-02).
 * Todas as contas de demonstração usam a senha definida em SENHA_DEMO (ver README).
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    public static final String SENHA_DEMO = "sdpp123";

    private final CompetenciaRepository competenciaRepository;
    private final QuadranteRepository quadranteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final CicloAvaliacaoRepository cicloRepository;
    private final CicloCompetenciaRepository cicloCompetenciaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PontuacaoRepository pontuacaoRepository;
    private final NineBoxRepository nineBoxRepository;
    private final PDIRepository pdiRepository;
    private final AcaoPDIRepository acaoRepository;
    private final PesquisaClimaRepository pesquisaRepository;
    private final PerguntaRepository perguntaRepository;
    private final ParticipacaoClimaRepository participacaoRepository;
    private final RespostaRepository respostaRepository;
    private final PasswordEncoder passwordEncoder;
    private final SdppProperties props;

    public DataInitializer(CompetenciaRepository competenciaRepository, QuadranteRepository quadranteRepository,
                           UsuarioRepository usuarioRepository, DepartamentoRepository departamentoRepository,
                           CargoRepository cargoRepository, ColaboradorRepository colaboradorRepository,
                           CicloAvaliacaoRepository cicloRepository,
                           CicloCompetenciaRepository cicloCompetenciaRepository,
                           AvaliacaoRepository avaliacaoRepository, PontuacaoRepository pontuacaoRepository,
                           NineBoxRepository nineBoxRepository, PDIRepository pdiRepository,
                           AcaoPDIRepository acaoRepository, PesquisaClimaRepository pesquisaRepository,
                           PerguntaRepository perguntaRepository, ParticipacaoClimaRepository participacaoRepository,
                           RespostaRepository respostaRepository, PasswordEncoder passwordEncoder,
                           SdppProperties props) {
        this.competenciaRepository = competenciaRepository;
        this.quadranteRepository = quadranteRepository;
        this.usuarioRepository = usuarioRepository;
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.cicloRepository = cicloRepository;
        this.cicloCompetenciaRepository = cicloCompetenciaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.pontuacaoRepository = pontuacaoRepository;
        this.nineBoxRepository = nineBoxRepository;
        this.pdiRepository = pdiRepository;
        this.acaoRepository = acaoRepository;
        this.pesquisaRepository = pesquisaRepository;
        this.perguntaRepository = perguntaRepository;
        this.participacaoRepository = participacaoRepository;
        this.respostaRepository = respostaRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (competenciaRepository.count() == 0) {
            carregarCompetencias();
        }
        if (quadranteRepository.count() == 0) {
            carregarQuadrantes();
        }
        if (props.seed().demo() && usuarioRepository.count() == 0) {
            carregarDemonstracao();
            log.info("Dados de demonstração carregados (contas em README.md).");
        }
    }

    // ---------- Catálogo de competências ([P1], Anexo A.2) ----------

    private void carregarCompetencias() {
        String[][] comportamentais = {
                {"Comunicação", "Saber passar informações de forma clara e simples, falando ou escrevendo, e saber ouvir com atenção."},
                {"Pensamento crítico", "Analisar informações com cuidado, entender o que está acontecendo e pensar em soluções bem fundamentadas."},
                {"Resolução de problemas", "Identificar o problema, entender a causa e encontrar a melhor forma de resolver."},
                {"Trabalho em equipe", "Colaborar com colegas, trocar ideias e trabalhar junto para atingir os objetivos."},
                {"Adaptabilidade", "Conseguir se ajustar a mudanças, desafios e imprevistos sem perder o foco."},
                {"Liderança", "Inspirar e motivar outras pessoas, ajudando a orientar o trabalho e alcançar resultados."},
                {"Inteligência emocional", "Reconhecer e controlar as próprias emoções, entendendo também as dos outros, para manter boas relações."},
                {"Criatividade", "Ter ideias diferentes e buscar novas formas de resolver problemas."},
                {"Ética", "Agir de forma correta, honesta e respeitosa em todas as situações."},
                {"Organização", "Planejar e priorizar tarefas para cumprir prazos e manter tudo em ordem."}};
        String[][] tecnicas = {
                {"Análise de dados", "Coletar e interpretar informações para entender padrões e ajudar na tomada de decisões."},
                {"Gestão de projetos", "Planejar e acompanhar tarefas e prazos para garantir que um projeto seja concluído com sucesso."},
                {"Planilhas", "Utilizar planilhas para organizar, calcular e analisar informações."},
                {"NR-35", "Conhecer e aplicar as regras de segurança para trabalho em altura."},
                {"Fluxo de caixa", "Registrar e acompanhar entradas e saídas de dinheiro para controlar as finanças."},
                {"Atendimento ao cliente", "Ajudar e orientar clientes de forma clara, educada e eficiente."},
                {"Pacote Office", "Usar programas como Word, Excel e PowerPoint no dia a dia."},
                {"Manutenção preventiva", "Fazer verificações e cuidados para evitar falhas em máquinas ou equipamentos."},
                {"Gestão de estoque", "Controlar entradas, saídas e organização de materiais."},
                {"Noções de segurança do trabalho", "Conhecer práticas para prevenir acidentes e manter um ambiente seguro."}};
        for (String[] c : comportamentais) {
            competencia(c[0], c[1], TipoCompetencia.COMPORTAMENTAL);
        }
        for (String[] c : tecnicas) {
            competencia(c[0], c[1], TipoCompetencia.TECNICA);
        }
    }

    private void competencia(String nome, String descricao, TipoCompetencia tipo) {
        CompetenciaModel c = new CompetenciaModel();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setTipo(tipo);
        competenciaRepository.save(c);
    }

    // ---------- Quadrantes da Nine Box ([P2], Anexo A.3) ----------

    private void carregarQuadrantes() {
        quadrante(Nivel.BAIXO, Nivel.BAIXO, "Insuficiente",
                "Entrega pouco hoje e não apresenta sinais de desenvolvimento futuro.",
                "Aplicar feedback estruturado com clareza sobre as expectativas do cargo.",
                "Avaliar se há desalinhamento de função, valores ou motivação.",
                "Propor plano de curto prazo com metas básicas e mensuráveis.",
                "Caso não haja progresso em até 3 meses, considerar realocação ou desligamento.");
        quadrante(Nivel.BAIXO, Nivel.MEDIO, "Questionável",
                "Ainda não entrega, mas demonstra algum potencial de evolução.",
                "Investigar barreiras de desempenho: é técnico, comportamental ou motivacional?",
                "Designar um mentor ou buddy de apoio.",
                "Estabelecer metas de curto prazo e acompanhamento quinzenal.",
                "Avaliar evolução em 3 a 6 meses antes de decidir permanência.");
        quadrante(Nivel.BAIXO, Nivel.ALTO, "Enigma",
                "Possui grande capacidade, mas não está entregando.",
                "Conversa franca sobre expectativas, ambições e possíveis desalinhamentos.",
                "Testar novo desafio ou contexto para estimular o engajamento (job rotation, por exemplo).",
                "Apoio com coaching ou acompanhamento próximo da liderança.",
                "Avaliar se a baixa performance é conjuntural ou estrutural (fit cultural, motivação, etc.).");
        quadrante(Nivel.MEDIO, Nivel.BAIXO, "Eficaz",
                "Entrega o necessário, mas não demonstra margem de crescimento.",
                "Estabelecer metas realistas e garantir boas condições de entrega.",
                "Reforçar reconhecimento pela estabilidade e confiabilidade.",
                "Evitar pressionar com desafios ou promoções que não condizem com o perfil.",
                "Apoiar no desenvolvimento técnico ou soft skills pontuais.");
        quadrante(Nivel.MEDIO, Nivel.MEDIO, "Mantenedor",
                "É constante, com algum espaço para crescer.",
                "Acompanhar com feedbacks regulares para manter o nível de entrega.",
                "Explorar oportunidades de desenvolvimento leve (cursos, responsabilidades moderadas).",
                "Incluir em iniciativas de melhoria contínua ou projetos em equipe.",
                "Avaliar interesse por crescimento futuro e caminhos possíveis.");
        quadrante(Nivel.MEDIO, Nivel.ALTO, "Em crescimento",
                "Tem bons resultados hoje e/ou capacidade futura de assumir mais.",
                "Identificar competências-chave para lapidar: técnica, liderança ou soft skills.",
                "Propor PDI com foco em aceleração.",
                "Delegar desafios crescentes e ampliar escopo gradualmente.",
                "Preparar para assumir cargos mais estratégicos no médio prazo.");
        quadrante(Nivel.ALTO, Nivel.BAIXO, "Comprometido",
                "Entrega muito bem, mas não deve assumir novos desafios.",
                "Valorizar o que já entrega: bônus, reconhecimento público e visibilidade.",
                "Evitar sobrecarga ou promoção indevida (risco do Princípio de Peter).",
                "Apoiar no aperfeiçoamento contínuo da função atual.",
                "Estimular compartilhamento de boas práticas com o time.");
        quadrante(Nivel.ALTO, Nivel.MEDIO, "Forte desempenho",
                "Resultado consistente com potencial de crescimento adicional.",
                "Desenhar trajetória de crescimento em conjunto (cargos, trilha técnica ou de liderança).",
                "Envolver em projetos estratégicos da empresa.",
                "Oferecer capacitações formais e desafiadoras.",
                "Mentorar outros colaboradores como etapa preparatória para promoção.");
        quadrante(Nivel.ALTO, Nivel.ALTO, "Alto potencial",
                "Performance excelente hoje e forte capacidade de liderar no futuro.",
                "Colocar em pipeline de sucessão com plano de desenvolvimento robusto.",
                "Oferecer coaching executivo e networking com lideranças.",
                "Expor à alta gestão e decisões estratégicas.",
                "Garantir reconhecimento, desafios e retenção ativa (salário, plano de carreira, propósito).");
    }

    private void quadrante(Nivel desempenho, Nivel potencial, String rotulo, String descricao, String... acoes) {
        QuadranteModel q = new QuadranteModel();
        q.setDesempenho(desempenho);
        q.setPotencial(potencial);
        q.setRotulo(rotulo);
        q.setDescricao(descricao);
        q.setPlanoAcao(String.join("\n", acoes));
        quadranteRepository.save(q);
    }

    // ---------- Dados fictícios de demonstração ----------

    private void carregarDemonstracao() {
        LocalDate hoje = LocalDate.now();
        var rh = departamento("Recursos Humanos");
        var ti = departamento("Tecnologia");
        var comercial = departamento("Comercial");
        departamento("Operações");

        var analistaRH = cargo("Analista de RH");
        var gerenteTI = cargo("Gerente de Tecnologia");
        var dev = cargo("Desenvolvedor(a)");
        var dados = cargo("Analista de Dados");
        var gerenteCom = cargo("Gerente Comercial");
        var vendedor = cargo("Executivo(a) de Vendas");

        var ana = pessoa("Ana Souza", "rh@sdpp.local", Perfil.RH, rh, analistaRH, null, hoje.minusYears(6));
        var carlos = pessoa("Carlos Lima", "gestor@sdpp.local", Perfil.GESTOR, ti, gerenteTI, null, hoje.minusYears(5));
        var fernanda = pessoa("Fernanda Costa", "gestora.comercial@sdpp.local", Perfil.GESTOR, comercial, gerenteCom,
                null, hoje.minusYears(4));
        var marina = pessoa("Marina Oliveira", "marina@sdpp.local", Perfil.COLABORADOR, ti, dev, carlos, hoje.minusYears(3));
        var pedro = pessoa("Pedro Santos", "pedro@sdpp.local", Perfil.COLABORADOR, ti, dados, carlos, hoje.minusYears(2));
        var joana = pessoa("Joana D'Ávila", "joana@sdpp.local", Perfil.COLABORADOR, ti, dev, carlos, hoje.minusYears(1));
        var lucas = pessoa("Lucas Rocha", "lucas@sdpp.local", Perfil.COLABORADOR, ti, dev, carlos, hoje.minusMonths(18));
        var beatriz = pessoa("Beatriz Alves", "beatriz@sdpp.local", Perfil.COLABORADOR, ti, dados, carlos, hoje.minusMonths(10));
        pessoa("Rafael Mendes", "rafael@sdpp.local", Perfil.COLABORADOR, comercial, vendedor, fernanda, hoje.minusMonths(30));
        carlos.setGestor(ana);
        fernanda.setGestor(ana);

        // Ciclo anterior, já com resultados liberados
        var anterior = ciclo("Ciclo 2025/2", hoje.minusMonths(6), hoje.minusMonths(4), StatusCiclo.RESULTADOS_LIBERADOS);
        var ccs = competenciasDoCiclo(anterior);
        // Marina: casos dourados CA-RF-AVA-08a/08b (gestor 4,1; autoavaliação 3,9)
        avaliar(anterior, ccs, marina, carlos, TipoAvaliacao.GESTOR, new int[]{4, 5, 3, 4, 4, 5, 3, 4, 4, 5});
        avaliar(anterior, ccs, marina, marina, TipoAvaliacao.AUTOAVALIACAO, new int[]{4, 4, 4, 4, 4, 3, 3, 4, 4, 5});
        avaliar(anterior, ccs, pedro, carlos, TipoAvaliacao.GESTOR, new int[]{5, 4, 5, 4, 4, 5, 5, 4, 5, 4});
        avaliar(anterior, ccs, pedro, pedro, TipoAvaliacao.AUTOAVALIACAO, new int[]{4, 4, 5, 4, 4, 4, 5, 4, 4, 4});
        avaliar(anterior, ccs, joana, carlos, TipoAvaliacao.GESTOR, new int[]{2, 3, 2, 2, 3, 2, 1, 2, 3, 2});
        avaliar(anterior, ccs, joana, joana, TipoAvaliacao.AUTOAVALIACAO, new int[]{4, 3, 3, 4, 3, 3, 3, 3, 4, 3});
        avaliar(anterior, ccs, lucas, carlos, TipoAvaliacao.GESTOR, new int[]{3, 3, 4, 3, 3, 3, 4, 3, 3, 3});
        // Nine Box: exemplos de [P2] (CA-RF-NBX-02a); Lucas e Beatriz ficam pendentes
        nineBox(anterior, marina, Nivel.MEDIO, Nivel.MEDIO, carlos);
        nineBox(anterior, pedro, Nivel.ALTO, Nivel.MEDIO, carlos);
        nineBox(anterior, joana, Nivel.BAIXO, Nivel.ALTO, carlos);

        // Ciclo atual, liberado para avaliações
        var atual = ciclo("Ciclo 2026/1", hoje.minusDays(15), hoje.plusDays(45), StatusCiclo.LIBERADO);
        competenciasDoCiclo(atual);

        // PDI da Joana, criado pelo gestor (ação manual, RN-04)
        PDIModel pdi = new PDIModel();
        pdi.setColaborador(joana);
        pdi.setGestor(carlos);
        pdi.setCiclo(anterior);
        pdi.setObjetivo("Aumentar a autonomia na entrega de funcionalidades e a qualidade do código.");
        pdi.setHabilidadesDesenvolver("Organização\nResolução de problemas\nComunicação com o time");
        pdi.setHabilidadesSucesso("Criatividade\nAprendizado rápido");
        pdi.setAnotacoes("Conversa de feedback realizada após a liberação dos resultados.");
        pdi.setCriadoEm(hoje.minusMonths(3));
        pdi = pdiRepository.save(pdi);
        acao(pdi, "Curso de boas práticas de código", hoje.minusMonths(3), hoje.minusMonths(1),
                "Certificado de conclusão", "Curso online - R$ 51,35", StatusAcaoPDI.CONCLUIDO, 100);
        acao(pdi, "Mentoria quinzenal com o Pedro", hoje.minusMonths(2), hoje.minusDays(5),
                "Registro das sessões", "Grátis", StatusAcaoPDI.EM_ANDAMENTO, 60);
        acao(pdi, "Liderar a entrega de uma funcionalidade", hoje, hoje.plusMonths(2),
                "Funcionalidade em produção", "Horas do time", StatusAcaoPDI.A_FAZER, 0);

        // Pesquisa de clima publicada, já respondida pelos 5 colaboradores de Tecnologia
        PesquisaClimaModel pesquisa = new PesquisaClimaModel();
        pesquisa.setTitulo("Pesquisa de Clima 2026");
        pesquisa.setDataInicio(hoje.minusDays(10));
        pesquisa.setDataFim(hoje.plusDays(20));
        pesquisa.setStatus(StatusPesquisa.PUBLICADA);
        pesquisa = pesquisaRepository.save(pesquisa);
        List<PerguntaModel> perguntas = new ArrayList<>();
        int ordem = 1;
        for (var q : QuestionarioPadrao.PERGUNTAS) {
            PerguntaModel p = new PerguntaModel();
            p.setPesquisa(pesquisa);
            p.setTexto(q.texto());
            p.setTipo(q.tipo());
            p.setObrigatoria(true);
            p.setOrdem(ordem++);
            perguntas.add(perguntaRepository.save(p));
        }
        int[][] valores = {{4, 5, 3, 4, 4}, {5, 4, 4, 3, 5}, {3, 4, 5, 4, 3}, {4, 3, 4, 5, 4}, {5, 5, 4, 4, 3}};
        List<ColaboradorModel> respondentes = List.of(marina, pedro, joana, lucas, beatriz);
        List<RespostaModel> respostas = new ArrayList<>();
        for (int i = 0; i < respondentes.size(); i++) {
            ParticipacaoClimaModel part = new ParticipacaoClimaModel();
            part.setPesquisa(pesquisa);
            part.setColaborador(respondentes.get(i));
            part.setDataParticipacao(hoje.minusDays(5));
            participacaoRepository.save(part);
            for (int j = 0; j < perguntas.size(); j++) {
                RespostaModel r = new RespostaModel();
                r.setPergunta(perguntas.get(j));
                r.setSetor(ti.getNome());
                if (perguntas.get(j).getTipo() == TipoPergunta.ESCALA) {
                    r.setValor(valores[i][j % 5]);
                }
                respostas.add(r);
            }
        }
        Collections.shuffle(respostas);
        respostaRepository.saveAll(respostas);
    }

    private DepartamentoModel departamento(String nome) {
        DepartamentoModel d = new DepartamentoModel();
        d.setNome(nome);
        return departamentoRepository.save(d);
    }

    private CargoModel cargo(String nome) {
        CargoModel c = new CargoModel();
        c.setNome(nome);
        return cargoRepository.save(c);
    }

    private ColaboradorModel pessoa(String nome, String email, Perfil perfil, DepartamentoModel d, CargoModel cargo,
                                    ColaboradorModel gestor, LocalDate admissao) {
        UsuarioModel u = new UsuarioModel();
        u.setNome(nome);
        u.setEmail(email);
        u.setPerfil(perfil);
        u.setSenha(passwordEncoder.encode(SENHA_DEMO));
        u = usuarioRepository.save(u);
        ColaboradorModel c = new ColaboradorModel();
        c.setNome(nome);
        c.setEmail(email);
        c.setDepartamento(d);
        c.setCargo(cargo);
        c.setGestor(gestor);
        c.setDataAdmissao(admissao);
        c.setUsuario(u);
        return colaboradorRepository.save(c);
    }

    private CicloAvaliacaoModel ciclo(String nome, LocalDate inicio, LocalDate fim, StatusCiclo status) {
        CicloAvaliacaoModel c = new CicloAvaliacaoModel();
        c.setNome(nome);
        c.setDataInicio(inicio);
        c.setDataFim(fim);
        c.setStatus(status);
        return cicloRepository.save(c);
    }

    /** Seleciona as 5 primeiras competências de cada tipo; retorna na ordem tipo/nome. */
    private List<CicloCompetenciaModel> competenciasDoCiclo(CicloAvaliacaoModel ciclo) {
        List<CompetenciaModel> catalogo = competenciaRepository.findAll();
        for (TipoCompetencia tipo : TipoCompetencia.values()) {
            catalogo.stream().filter(c -> c.getTipo() == tipo).limit(5).forEach(c -> {
                CicloCompetenciaModel cc = new CicloCompetenciaModel();
                cc.setCiclo(ciclo);
                cc.setCompetencia(c);
                cicloCompetenciaRepository.save(cc);
            });
        }
        return cicloCompetenciaRepository.findByCicloIdOrderByCompetenciaTipoAscCompetenciaNomeAsc(ciclo.getId());
    }

    private void avaliar(CicloAvaliacaoModel ciclo, List<CicloCompetenciaModel> ccs, ColaboradorModel avaliado,
                         ColaboradorModel avaliador, TipoAvaliacao tipo, int[] notas) {
        AvaliacaoModel a = new AvaliacaoModel();
        a.setCiclo(ciclo);
        a.setColaborador(avaliado);
        a.setAvaliador(avaliador);
        a.setTipo(tipo);
        a.setStatus(StatusAvaliacao.CONCLUIDA);
        a.setDataAvaliacao(ciclo.getDataFim().minusDays(7));
        a = avaliacaoRepository.save(a);
        for (int i = 0; i < ccs.size(); i++) {
            PontuacaoModel p = new PontuacaoModel();
            p.setAvaliacao(a);
            p.setCicloCompetencia(ccs.get(i));
            p.setNota(notas[i]);
            pontuacaoRepository.save(p);
        }
    }

    private void nineBox(CicloAvaliacaoModel ciclo, ColaboradorModel c, Nivel desempenho, Nivel potencial,
                         ColaboradorModel gestor) {
        NineBoxModel nb = new NineBoxModel();
        nb.setCiclo(ciclo);
        nb.setColaborador(c);
        nb.setDesempenho(desempenho);
        nb.setPotencial(potencial);
        nb.setRegistradoPor(gestor);
        nb.setQuadrante(quadranteRepository.findByDesempenhoAndPotencial(desempenho, potencial).orElseThrow());
        nineBoxRepository.save(nb);
    }

    private void acao(PDIModel pdi, String descricao, LocalDate inicio, LocalDate termino, String entrega,
                      String recursos, StatusAcaoPDI status, int progresso) {
        AcaoPDIModel a = new AcaoPDIModel();
        a.setPdi(pdi);
        a.setDescricao(descricao);
        a.setDataInicio(inicio);
        a.setDataTermino(termino);
        a.setEntrega(entrega);
        a.setRecursos(recursos);
        a.setStatus(status);
        a.setProgresso(progresso);
        acaoRepository.save(a);
    }
}
