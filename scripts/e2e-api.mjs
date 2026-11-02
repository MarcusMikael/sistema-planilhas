// Teste ponta a ponta da API do SDPP contra o servidor real (banco de demonstração recém-criado).
// Uso: node scripts/e2e-api.mjs [baseUrl]   (backend rodando com banco de demonstração recém-criado)
const BASE = (process.argv[2] || 'http://localhost:8080') + '/api';
const SENHA = 'sdpp123';
let ok = 0;
const falhas = [];

function check(nome, cond, detalhe = '') {
  if (cond) ok++;
  else falhas.push(`${nome}${detalhe ? ' -> ' + detalhe : ''}`);
}

async function req(token, method, path, body) {
  const r = await fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const txt = await r.text();
  let data = null;
  try { data = txt ? JSON.parse(txt) : null; } catch { data = txt; }
  return { status: r.status, data };
}

async function login(email) {
  const r = await req(null, 'POST', '/auth/login', { email, senha: SENHA });
  if (r.status !== 200) throw new Error('login falhou ' + email + ' ' + r.status);
  return r.data.token;
}

function esperar(nome, r, status) {
  check(`${nome} [${status}]`, r.status === status, `obtido ${r.status} ${JSON.stringify(r.data)?.slice(0, 160)}`);
  return r.data;
}

const T = {};
for (const [k, e] of Object.entries({
  rh: 'rh@sdpp.local', carlos: 'gestor@sdpp.local', fernanda: 'gestora.comercial@sdpp.local',
  marina: 'marina@sdpp.local', pedro: 'pedro@sdpp.local', joana: 'joana@sdpp.local', lucas: 'lucas@sdpp.local',
  beatriz: 'beatriz@sdpp.local', rafael: 'rafael@sdpp.local',
})) T[k] = await login(e);

// ---------- Matriz de permissões (amostra por rota e perfil) ----------
const colabs = esperar('RH lista colaboradores', await req(T.rh, 'GET', '/colaboradores'), 200);
const id = (nome) => colabs.find((c) => c.nome === nome).id;
const rotasSoRH = [['GET', '/usuarios'], ['POST', '/colaboradores', {}], ['POST', '/ciclos', {}],
  ['POST', '/competencias', {}], ['POST', '/departamentos', { nome: 'X' }], ['POST', '/pesquisas-clima', {}]];
for (const [m, p, b] of rotasSoRH) {
  for (const quem of ['carlos', 'marina']) {
    const r = await req(T[quem], m, p, b);
    check(`${quem} ${m} ${p} bloqueado`, r.status === 403, `obtido ${r.status}`);
  }
}
check('sem token -> 401', (await req(null, 'GET', '/indicadores')).status === 401);
check('marina vê só ela', (await req(T.marina, 'GET', '/colaboradores')).data.length === 1);
check('carlos vê ele + 5', (await req(T.carlos, 'GET', '/colaboradores')).data.length === 6);
check('fernanda não vê Marina', (await req(T.fernanda, 'GET', `/colaboradores/${id('Marina Oliveira')}`)).status === 403);
check('rafael não vê histórico do Pedro', (await req(T.rafael, 'GET', `/colaboradores/${id('Pedro Santos')}/historico`)).status === 403);

// ---------- Ciclo completo ----------
const catalogo = esperar('catálogo', await req(T.rh, 'GET', '/competencias'), 200);
const comp = catalogo.filter((c) => c.tipo === 'COMPORTAMENTAL').map((c) => c.id);
const tec = catalogo.filter((c) => c.tipo === 'TECNICA').map((c) => c.id);
const hoje = new Date().toISOString().slice(0, 10);
const fim = new Date(Date.now() + 30 * 864e5).toISOString().slice(0, 10);
esperar('ciclo com fim < início', await req(T.rh, 'POST', '/ciclos', { nome: 'X', dataInicio: fim, dataFim: hoje }), 422);
const ciclo = esperar('criar ciclo', await req(T.rh, 'POST', '/ciclos', { nome: 'E2E 2026/2', dataInicio: hoje, dataFim: fim }), 201);
check('ciclo nasce em rascunho', ciclo.status === 'RASCUNHO');
check('rascunho invisível ao colaborador', (await req(T.marina, 'GET', `/ciclos/${ciclo.id}`)).status === 404);
esperar('liberar sem competências', await req(T.rh, 'POST', `/ciclos/${ciclo.id}/liberar`), 422);
esperar('6 comportamentais', await req(T.rh, 'PUT', `/ciclos/${ciclo.id}/competencias`, { competenciaIds: comp.slice(0, 6) }), 422);
esperar('competência repetida', await req(T.rh, 'PUT', `/ciclos/${ciclo.id}/competencias`, { competenciaIds: [comp[0], comp[0]] }), 422);
esperar('competência inexistente', await req(T.rh, 'PUT', `/ciclos/${ciclo.id}/competencias`, { competenciaIds: [999999] }), 404);
const sel = esperar('selecionar 5+5', await req(T.rh, 'PUT', `/ciclos/${ciclo.id}/competencias`, { competenciaIds: [...comp.slice(5, 10), ...tec.slice(5, 10)] }), 200);
check('5+5 contados', sel.qtdComportamentais === 5 && sel.qtdTecnicas === 5);
esperar('autoavaliação antes de liberar', await req(T.marina, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id('Marina Oliveira'), tipo: 'AUTOAVALIACAO' }), 422);
esperar('liberar ciclo', await req(T.rh, 'POST', `/ciclos/${ciclo.id}/liberar`), 200);
esperar('alterar competências após liberar', await req(T.rh, 'PUT', `/ciclos/${ciclo.id}/competencias`, { competenciaIds: [] }), 422);
esperar('excluir ciclo liberado', await req(T.rh, 'DELETE', `/ciclos/${ciclo.id}`), 422);

const equipe = ['Marina Oliveira', 'Pedro Santos', 'Joana D\'Ávila', 'Lucas Rocha', 'Beatriz Alves'];
const tokenDe = { 'Marina Oliveira': T.marina, 'Pedro Santos': T.pedro, 'Joana D\'Ávila': T.joana, 'Lucas Rocha': T.lucas, 'Beatriz Alves': T.beatriz };
const notasGestor = { 'Marina Oliveira': [4, 5, 3, 4, 4, 5, 3, 4, 4, 5], 'Pedro Santos': [5, 5, 5, 5, 5, 4, 4, 4, 4, 4],
  'Joana D\'Ávila': [1, 2, 2, 2, 1, 2, 2, 3, 2, 2], 'Lucas Rocha': [3, 3, 3, 3, 3, 3, 3, 3, 3, 3], 'Beatriz Alves': [4, 4, 4, 4, 4, 4, 4, 4, 4, 4] };

async function preencher(token, av, notas) {
  return req(token, 'PUT', `/avaliacoes/${av.id}/notas`, {
    notas: av.itens.map((it, i) => ({ cicloCompetenciaId: it.cicloCompetenciaId, nota: notas[i] })),
  });
}

for (const nome of equipe) {
  const t = tokenDe[nome];
  const auto = esperar(`${nome} abre autoavaliação`, await req(t, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id(nome), tipo: 'AUTOAVALIACAO' }), 200);
  check(`${nome} autoavaliação com 10 itens`, auto.itens.length === 10);
  const repetida = await req(t, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id(nome), tipo: 'AUTOAVALIACAO' });
  check(`${nome} reabrir retorna a mesma`, repetida.data.id === auto.id);
  esperar(`${nome} nota -1`, await req(t, 'PUT', `/avaliacoes/${auto.id}/notas`, { notas: [{ cicloCompetenciaId: auto.itens[0].cicloCompetenciaId, nota: -1 }] }), 400);
  esperar(`${nome} preenche`, await preencher(t, auto, [4, 4, 4, 4, 4, 3, 3, 4, 4, 5]), 200);
  esperar(`${nome} conclui`, await req(t, 'POST', `/avaliacoes/${auto.id}/concluir`), 200);
  esperar(`${nome} alterar após concluir`, await preencher(t, auto, [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]), 422);

  const g = esperar(`Carlos abre avaliação de ${nome}`, await req(T.carlos, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id(nome), tipo: 'GESTOR' }), 200);
  check(`avaliações independentes (${nome})`, g.id !== auto.id);
  esperar(`${nome} não edita avaliação do gestor`, await preencher(t, g, notasGestor[nome]), 403);
  // Parcial: conclusão bloqueada (RN-02)
  esperar(`Carlos salva 9 notas de ${nome}`, await preencher(T.carlos, g, [...notasGestor[nome].slice(0, 9), null]), 200);
  esperar(`Carlos não conclui incompleta`, await req(T.carlos, 'POST', `/avaliacoes/${g.id}/concluir`), 422);
  esperar(`Carlos preenche ${nome}`, await preencher(T.carlos, g, notasGestor[nome]), 200);
  esperar(`Carlos conclui ${nome}`, await req(T.carlos, 'POST', `/avaliacoes/${g.id}/concluir`), 200);
}
esperar('Fernanda não avalia equipe do Carlos', await req(T.fernanda, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id('Pedro Santos'), tipo: 'GESTOR' }), 403);
// Ana (RH) avalia Carlos, seu subordinado direto
const avCarlos = esperar('Ana abre avaliação do Carlos', await req(T.rh, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id('Carlos Lima'), tipo: 'GESTOR' }), 200);
esperar('Ana preenche Carlos', await preencher(T.rh, avCarlos, [5, 4, 5, 4, 5, 4, 5, 4, 5, 4]), 200);
esperar('Ana conclui Carlos', await req(T.rh, 'POST', `/avaliacoes/${avCarlos.id}/concluir`), 200);

// Resultados antes da liberação
esperar('Marina não vê resultado antes', await req(T.marina, 'GET', `/ciclos/${ciclo.id}/resultados/${id('Marina Oliveira')}`), 403);
esperar('Carlos não vê resultado antes', await req(T.carlos, 'GET', `/ciclos/${ciclo.id}/resultados/${id('Marina Oliveira')}`), 403);
const rhRes = esperar('RH vê antes da liberação', await req(T.rh, 'GET', `/ciclos/${ciclo.id}/resultados/${id('Marina Oliveira')}`), 200);
check('Média gestor Marina 4,1', rhRes.gestor.geral === 4.1, JSON.stringify(rhRes.gestor));
check('Média auto Marina 3,9', rhRes.autoavaliacao.geral === 3.9, JSON.stringify(rhRes.autoavaliacao));
esperar('Nine Box antes da liberação', await req(T.carlos, 'PUT', '/nine-box', { cicloId: ciclo.id, colaboradorId: id('Pedro Santos'), potencial: 'ALTO' }), 422);

esperar('Gestor não libera resultados', await req(T.carlos, 'POST', `/ciclos/${ciclo.id}/liberar-resultados`), 403);
esperar('liberar resultados', await req(T.rh, 'POST', `/ciclos/${ciclo.id}/liberar-resultados`), 200);
esperar('abrir avaliação após liberação', await req(T.fernanda, 'POST', '/avaliacoes', { cicloId: ciclo.id, colaboradorId: id('Rafael Mendes'), tipo: 'GESTOR' }), 422);

const resM = esperar('Marina vê o próprio', await req(T.marina, 'GET', `/ciclos/${ciclo.id}/resultados/${id('Marina Oliveira')}`), 200);
check('comparação com 10 itens', resM.itens.length === 10);
esperar('Marina não vê Pedro', await req(T.marina, 'GET', `/ciclos/${ciclo.id}/resultados/${id('Pedro Santos')}`), 403);
const mapa = esperar('mapa (gestor)', await req(T.carlos, 'GET', `/ciclos/${ciclo.id}/mapa-competencias`), 200);
check('mapa só com a equipe do Carlos', mapa.colaboradores.length === 5, mapa.colaboradores.length);
const mapaRH = esperar('mapa (RH)', await req(T.rh, 'GET', `/ciclos/${ciclo.id}/mapa-competencias`), 200);
check('mapa RH inclui Carlos', mapaRH.colaboradores.length === 6);
esperar('mapa (colaborador)', await req(T.marina, 'GET', `/ciclos/${ciclo.id}/mapa-competencias`), 403);

// ---------- Nine Box ----------
const nb = esperar('matriz gestor', await req(T.carlos, 'GET', `/nine-box?cicloId=${ciclo.id}`), 200);
const itemPedro = nb.itens.find((i) => i.colaboradorNome === 'Pedro Santos');
check('sugestão Pedro = ALTO', itemPedro.desempenhoSugerido === 'ALTO', JSON.stringify(itemPedro));
check('sugestão Joana = BAIXO', nb.itens.find((i) => i.colaboradorNome === "Joana D'Ávila").desempenhoSugerido === 'BAIXO');
check('sugestão Lucas = MEDIO', nb.itens.find((i) => i.colaboradorNome === 'Lucas Rocha').desempenhoSugerido === 'MEDIO');
check('todos pendentes', nb.itens.every((i) => i.pendente));
const reg = esperar('registrar Pedro', await req(T.carlos, 'PUT', '/nine-box', { cicloId: ciclo.id, colaboradorId: id('Pedro Santos'), desempenho: 'ALTO', potencial: 'ALTO' }), 200);
check('Pedro em Alto potencial', reg.quadrante?.rotulo === 'Alto potencial');
esperar('Fernanda não registra Pedro', await req(T.fernanda, 'PUT', '/nine-box', { cicloId: ciclo.id, colaboradorId: id('Pedro Santos'), desempenho: 'BAIXO', potencial: 'BAIXO' }), 403);
esperar('Ana registra Carlos', await req(T.rh, 'PUT', '/nine-box', { cicloId: ciclo.id, colaboradorId: id('Carlos Lima'), desempenho: 'ALTO', potencial: 'MEDIO' }), 200);
const nbRH = esperar('matriz RH', await req(T.rh, 'GET', `/nine-box?cicloId=${ciclo.id}`), 200);
check('RH vê Carlos como Forte desempenho', nbRH.itens.find((i) => i.colaboradorNome === 'Carlos Lima').quadrante?.rotulo === 'Forte desempenho');

// ---------- PDI ----------
const pdi = esperar('Carlos cria PDI do Lucas', await req(T.carlos, 'POST', '/pdis', { colaboradorId: id('Lucas Rocha'), cicloId: ciclo.id, objetivo: 'Evoluir para pleno', habilidadesDesenvolver: 'Comunicação' }), 201);
esperar('Fernanda não cria PDI do Lucas', await req(T.fernanda, 'POST', '/pdis', { colaboradorId: id('Lucas Rocha'), objetivo: 'x' }), 403);
esperar('Lucas não cria PDI', await req(T.lucas, 'POST', '/pdis', { colaboradorId: id('Lucas Rocha'), objetivo: 'x' }), 403);
esperar('PDI sem objetivo', await req(T.carlos, 'POST', '/pdis', { colaboradorId: id('Lucas Rocha'), objetivo: '' }), 400);
esperar('ação com término antes', await req(T.carlos, 'POST', `/pdis/${pdi.id}/acoes`, { descricao: 'Curso', dataInicio: '2026-03-10', dataTermino: '2026-03-05' }), 422);
const acao = esperar('ação válida', await req(T.carlos, 'POST', `/pdis/${pdi.id}/acoes`, { descricao: 'Curso', dataInicio: '2026-03-10', dataTermino: '2026-03-10', entrega: 'Certificado', recursos: 'R$ 51,35' }), 201);
esperar('Marina não atualiza ação do Lucas', await req(T.marina, 'PATCH', `/acoes-pdi/${acao.id}/progresso`, { status: 'EM_ANDAMENTO', progresso: 10 }), 403);
const prog = esperar('Lucas atualiza progresso', await req(T.lucas, 'PATCH', `/acoes-pdi/${acao.id}/progresso`, { status: 'CONCLUIDO', progresso: 30 }), 200);
check('concluído vira 100%', prog.progresso === 100);
esperar('progresso 150', await req(T.lucas, 'PATCH', `/acoes-pdi/${acao.id}/progresso`, { status: 'EM_ANDAMENTO', progresso: 150 }), 400);
esperar('Lucas não edita a ação', await req(T.lucas, 'PUT', `/acoes-pdi/${acao.id}`, { descricao: 'x', dataInicio: '2026-01-01', dataTermino: '2026-01-02' }), 403);
const pdiLucas = esperar('Lucas vê seu PDI', await req(T.lucas, 'GET', `/pdis/${pdi.id}`), 200);
check('progresso geral 100', pdiLucas.progressoGeral === 100);
esperar('Marina não vê PDI do Lucas', await req(T.marina, 'GET', `/pdis/${pdi.id}`), 403);
esperar('transferir PDI', await req(T.carlos, 'PUT', `/pdis/${pdi.id}`, { colaboradorId: id('Marina Oliveira'), objetivo: 'x' }), 422);

// ---------- Pesquisa de clima ----------
const p = esperar('RH cria campanha', await req(T.rh, 'POST', '/pesquisas-clima', { titulo: 'Clima E2E', dataInicio: hoje, dataFim: fim, usarQuestionarioPadrao: true }), 201);
check('20 perguntas padrão', p.perguntas.length === 20);
check('rascunho invisível ao colaborador', (await req(T.marina, 'GET', `/pesquisas-clima/${p.id}`)).status === 404);
const extra = esperar('adiciona pergunta aberta', await req(T.rh, 'POST', `/pesquisas-clima/${p.id}/perguntas`, { texto: 'Sugestões?', tipo: 'TEXTO', obrigatoria: false }), 201);
esperar('responder rascunho', await req(T.marina, 'POST', `/pesquisas-clima/${p.id}/respostas`, { respostas: [] }), 422);
esperar('publicar', await req(T.rh, 'POST', `/pesquisas-clima/${p.id}/publicar`), 200);
esperar('editar pergunta publicada', await req(T.rh, 'PUT', `/pesquisas-clima/perguntas/${extra.id}`, { texto: 'x', tipo: 'TEXTO' }), 422);
const det = (await req(T.marina, 'GET', `/pesquisas-clima/${p.id}`)).data;
const escala = det.perguntas.filter((q) => q.tipo === 'ESCALA');
const respostas = (v) => ({ respostas: [...escala.map((q) => ({ perguntaId: q.id, valor: v })), { perguntaId: extra.id, texto: 'Mais treinamentos' }] });
esperar('faltando obrigatória', await req(T.marina, 'POST', `/pesquisas-clima/${p.id}/respostas`, { respostas: [{ perguntaId: escala[0].id, valor: 3 }] }), 422);
esperar('valor 6', await req(T.marina, 'POST', `/pesquisas-clima/${p.id}/respostas`, respostas(6)), 422);
const valores = { marina: 4, pedro: 5, joana: 3, lucas: 4, beatriz: 4, rafael: 2 };
for (const [quem, v] of Object.entries(valores)) {
  esperar(`${quem} responde`, await req(T[quem], 'POST', `/pesquisas-clima/${p.id}/respostas`, respostas(v)), 204);
}
esperar('Marina responde de novo', await req(T.marina, 'POST', `/pesquisas-clima/${p.id}/respostas`, respostas(4)), 409);
esperar('gestor não vê resultados', await req(T.carlos, 'GET', `/pesquisas-clima/${p.id}/resultados`), 403);
const res = esperar('RH vê resultados', await req(T.rh, 'GET', `/pesquisas-clima/${p.id}/resultados`), 200);
check('6 participações', res.totalParticipacoes === 6);
const q2 = res.perguntas.find((q) => q.ordem === 2);
check('média geral (4+5+3+4+4+2)/6 = 3,67', q2.media === 3.67, q2.media);
check('Tecnologia exibida com média 4,0', q2.porSetor.find((s) => s.setor === 'Tecnologia')?.media === 4.0, JSON.stringify(q2.porSetor));
const com = q2.porSetor.find((s) => s.setor === 'Comercial');
check('Comercial (1 resposta) oculto', com && !com.exibido && com.media === null, JSON.stringify(com));
check('textos exibidos (6 >= 5)', res.perguntas.find((q) => q.perguntaId === extra.id).respostasTexto?.length === 6);
const bruto = JSON.stringify(res);
check('resultado sem identificação', !/marina|pedro|colaborador/i.test(bruto));
check('rota de respostas individuais inexistente', (await req(T.rh, 'GET', '/respostas')).status === 404);
esperar('encerrar', await req(T.rh, 'POST', `/pesquisas-clima/${p.id}/encerrar`), 200);
esperar('responder encerrada', await req(T.carlos, 'POST', `/pesquisas-clima/${p.id}/respostas`, respostas(3)), 422);

// ---------- Indicadores e histórico ----------
const indG = esperar('indicadores gestor', await req(T.carlos, 'GET', '/indicadores'), 200);
check('indicadores do ciclo atual (E2E)', indG.cicloAtual?.nome === 'E2E 2026/2', indG.cicloAtual?.nome);
check('5/5 avaliações do gestor', indG.avaliacoesGestorConcluidas === 5 && indG.autoavaliacoesConcluidas === 5);
check('1 classificado na Nine Box', indG.pendentesNineBox === 4);
const hist = esperar('histórico Lucas (gestor)', await req(T.carlos, 'GET', `/colaboradores/${id('Lucas Rocha')}/historico`), 200);
check('histórico com 3 ciclos', hist.ciclos.length === 3, hist.ciclos.length);
check('histórico com PDI', hist.pdis.length === 1);
const histL = esperar('histórico Lucas (ele mesmo)', await req(T.lucas, 'GET', `/colaboradores/${id('Lucas Rocha')}/historico`), 200);
check('Lucas não vê Nine Box', histL.ciclos.every((c) => c.quadrante === null));

// ---------- Cadastros ----------
const dep = esperar('criar departamento', await req(T.rh, 'POST', '/departamentos', { nome: 'Financeiro' }), 201);
esperar('departamento duplicado', await req(T.rh, 'POST', '/departamentos', { nome: 'financeiro' }), 409);
esperar('excluir departamento em uso', await req(T.rh, 'DELETE', `/departamentos/${colabs[0].departamentoId}`), 409);
esperar('excluir departamento livre', await req(T.rh, 'DELETE', `/departamentos/${dep.id}`), 204);
const cargos = (await req(T.rh, 'GET', '/cargos')).data;
esperar('gestor = o próprio colaborador', await req(T.rh, 'PUT', `/colaboradores/${id('Carlos Lima')}`, {
  nome: 'Carlos Lima', email: 'gestor@sdpp.local', cargoId: cargos[0].id, departamentoId: colabs[0].departamentoId,
  gestorId: id('Marina Oliveira'), dataAdmissao: '2020-01-01' }), 422);
esperar('admissão no futuro', await req(T.rh, 'POST', '/colaboradores', { nome: 'F', email: 'f@sdpp.local', cargoId: cargos[0].id,
  departamentoId: colabs[0].departamentoId, dataAdmissao: '2099-01-01' }), 400);
esperar('e-mail inválido', await req(T.rh, 'POST', '/usuarios', { nome: 'X', email: 'x', senha: '123456', perfil: 'RH' }), 400);
esperar('perfil inexistente', await req(T.rh, 'POST', '/usuarios', { nome: 'X', email: 'x@sdpp.local', senha: '123456', perfil: 'ADMIN' }), 400);
esperar('senha curta', await req(T.rh, 'POST', '/usuarios', { nome: 'X', email: 'x@sdpp.local', senha: '123', perfil: 'RH' }), 400);
esperar('vincular colaborador que já tem conta', await req(T.rh, 'POST', '/usuarios', { nome: 'X', email: 'x2@sdpp.local', senha: '123456', perfil: 'COLABORADOR', colaboradorId: id('Pedro Santos') }), 409);
esperar('troca de senha com senha atual errada', await req(T.pedro, 'POST', '/auth/senha', { senhaAtual: 'errada', novaSenha: '1234567' }), 422);
esperar('rota inexistente', await req(T.rh, 'GET', '/nao-existe'), 404);
esperar('JSON inválido', { status: (await fetch(BASE + '/ciclos', { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + T.rh }, body: '{' })).status, data: null }, 400);

console.log(`\n${ok} verificações OK, ${falhas.length} falha(s)`);
falhas.forEach((f) => console.log('  FALHA: ' + f));
process.exit(falhas.length ? 1 : 0);
