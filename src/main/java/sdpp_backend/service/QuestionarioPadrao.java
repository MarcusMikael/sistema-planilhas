package sdpp_backend.service;

import java.util.List;

import sdpp_backend.model.enums.TipoPergunta;

/**
 * Questionário de clima de referência ([P4], Anexo E do DRS) – carga inicial de RF-CLI-01.
 * A pergunta 1 (setor) é preenchida pelo cadastro (PA-23). As demais usam a escala de 1 a 5;
 * o DRS não define a escala de 12, 13, 16, 19 e 20 (PA-04), então o RH pode ajustá-las antes de publicar.
 */
public final class QuestionarioPadrao {

    private QuestionarioPadrao() {
    }

    public record Pergunta(String texto, TipoPergunta tipo) {
    }

    public static final List<Pergunta> PERGUNTAS = List.of(
            new Pergunta("Qual é o setor que você trabalha?", TipoPergunta.SETOR),
            escala("Qual é o seu nível de satisfação em relação às tarefas que realiza diariamente?"),
            escala("Qual é a sua avaliação em relação ao relacionamento que tem com seu líder?"),
            escala("Como está a sua satisfação com respeito a liberdade de expressão na organização?"),
            escala("O que acha do lugar em que trabalha (tecnologias, espaço, equipamentos etc.)?"),
            escala("Os benefícios oferecidos pela empresa estão alinhados com as suas necessidades e expectativas?"),
            escala("Qual é o seu nível de satisfação com respeito ao relacionamento com os colegas de trabalho?"),
            escala("Gosta do plano de carreira oferecido pela empresa?"),
            escala("E a rotina de trabalho, o que acha dela?"),
            escala("Como está o seu nível de satisfação em relação a trabalhar nesta organização?"),
            escala("O que sente em relação aos feedbacks e avaliação de desempenho?"),
            escala("Em uma semana comum, como você avaliaria o seu nível de estresse?"),
            escala("Qual é a probabilidade de você procurar outro emprego?"),
            escala("As tarefas que o seu líder atribui a você ajudam no seu crescimento profissional?"),
            escala("Você sente que as suas sugestões são valorizadas?"),
            escala("Na sua visão, quantas oportunidades de ser promovida(o) você obteve nos últimos anos?"),
            escala("Você sente que a empresa ajuda no seu desenvolvimento profissional?"),
            escala("Como se sente em relação a cultura interna da empresa?"),
            escala("Considera os processos internos flexíveis ou engessados?"),
            escala("Você recomendaria a empresa como um bom lugar para trabalhar?"));

    private static Pergunta escala(String texto) {
        return new Pergunta(texto, TipoPergunta.ESCALA);
    }
}
