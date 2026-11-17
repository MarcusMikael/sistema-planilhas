# Equipes do SDPP

O SDPP é continuado por turmas diferentes ao longo dos períodos: cada equipe parte do trabalho da anterior
(por fork ou clone) e o amplia. Este arquivo guarda **o histórico de todas as equipes**, da mais recente para a
mais antiga, e acompanha cada cópia do projeto.

## Como registrar a sua equipe

1. Copie o [modelo](#modelo) abaixo e cole-o **no topo da lista de equipes**, logo abaixo de "Equipes".
2. Preencha período letivo, turma, disciplina, orientação, repositório, integrantes e entregas. Em **Partiu de**,
   coloque o repositório da equipe anterior: assim a cadeia entre as versões continua rastreável mesmo quando o
   projeto é clonado em vez de usar o botão *Fork*.
3. **Não remova nem altere as equipes anteriores:** elas são os créditos de quem construiu o que você recebeu
   (e a [licença MIT](LICENSE) exige manter o aviso de autoria).
4. Atualize também a seção *Equipe* do [README](README.md#equipe) com a sua equipe.

---

## Equipes

### 2026.2 · 6º período · Equipe fundadora

| Campo | Informação |
|---|---|
| **Período letivo** | 2026.2 |
| **Turma** | 6º período, turno manhã |
| **Disciplina** | Liderança e Gestão de Pessoas |
| **Curso e instituição** | Engenharia de Software – UNIFSA |
| **Orientação** | Prof. Aluysio Ricardo Nunes Fonseca |
| **Repositório** | https://github.com/MarcusMikael/sistema-planilhas |
| **Partiu de** | — (versão inicial) |
| **Sistema publicado** | https://sdpp-frontend-eta.vercel.app |

| Integrante | GitHub |
|---|---|
| Antonio Neves Aguiar Neto | [@Neeveszr](https://github.com/Neeveszr) |
| Marcos André dos Santos Soares | _a preencher_ |
| Carlos Arthur Moraes Gonçalves | _a preencher_ |
| João Pedro Lima Barbosa | _a preencher_ |
| Rodolfo Dheymison Ferreira Silva | _a preencher_ |
| João William Soares Teodomiro Setubal de Carvalho | _a preencher_ |
| Enzo Morais Gonçalves | _a preencher_ |

**Entregas do período:**

- sistema web completo a partir das quatro planilhas originais (Avaliação de Desempenho por Competência,
  Matriz Nine Box, PDI e Pesquisa de Clima), seguindo o DRS v0.2 e o Documento de Arquitetura;
- perfis RH, Gestor e Colaborador, com permissões por pessoa e equipe verificadas no backend;
- ciclos de avaliação, autoavaliação e avaliação do gestor, resultados com gráfico e mapa de competências,
  Nine Box, PDI, pesquisa de clima anônima, indicadores e histórico;
- segurança (sessão em cookie HttpOnly, proteção contra CSRF, limite de login), testes automatizados
  (backend, frontend e ponta a ponta) com integração contínua, e deploy na Vercel e no Render.

---

## Modelo

Copie a partir da linha `### …` e cole no topo da lista de equipes.

```markdown
### AAAA.S · Nº período · Nome da equipe (opcional)

| Campo | Informação |
|---|---|
| **Período letivo** | AAAA.S (ex.: 2027.1) |
| **Turma** | Nº período do curso e turno (ex.: 6º período, turno manhã) |
| **Disciplina** | Nome da disciplina |
| **Curso e instituição** | Curso – Instituição |
| **Orientação** | Prof. Nome do professor |
| **Repositório** | https://github.com/usuario/repositorio |
| **Partiu de** | Repositório da equipe anterior (de onde foi feito o fork ou clone) |
| **Sistema publicado** | URL, se houver |

| Integrante | GitHub |
|---|---|
| Nome do integrante | [@usuario](https://github.com/usuario) |

**Entregas do período:**

- o que a equipe acrescentou ou mudou no sistema;
- ...
```
