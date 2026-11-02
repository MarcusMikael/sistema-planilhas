import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Campo, Cartao, Carregando, Modal, Pagina, Seletor, Vazio } from '../components/ui.jsx';
import { NIVEIS, ROTULOS, fmtNum } from '../format.js';

// Cor do quadrante pela soma dos níveis (vermelho → verde)
function cor(d, p) {
  const s = NIVEIS.indexOf(d) + NIVEIS.indexOf(p);
  return ['q-r', 'q-a', 'q-a', 'q-v', 'q-vv'][s];
}

const opcoesNivel = NIVEIS.map((n) => ({ valor: n, rotulo: ROTULOS.nivel[n] }));

export default function NineBox() {
  const { isRH, isGestor, usuario } = useAuth();
  const [params, setParams] = useSearchParams();
  const ciclos = useApi('/ciclos');
  const depts = useApi(isRH ? '/departamentos' : null);
  const disponiveis = (ciclos.data || []).filter((c) => c.status !== 'RASCUNHO');
  const cicloId = params.get('ciclo') || disponiveis.find((c) => c.status === 'RESULTADOS_LIBERADOS')?.id || disponiveis[0]?.id;
  const dept = params.get('departamento');
  const matriz = useApi(cicloId ? `/nine-box?cicloId=${cicloId}${dept ? `&departamentoId=${dept}` : ''}` : null);
  const [quadrante, setQuadrante] = useState(null);
  const [registro, setRegistro] = useState(null);
  const acao = useAcao();

  if (ciclos.carregando) return <Carregando />;
  const m = matriz.data;
  const liberado = m?.cicloStatus === 'RESULTADOS_LIBERADOS';
  // Só o gestor responsável (definido no cadastro) registra a classificação
  const podeRegistrar = (i) => liberado && i.gestorId != null && i.gestorId === usuario.colaboradorId;
  const temEquipe = (m?.itens || []).some((i) => i.gestorId != null && i.gestorId === usuario.colaboradorId);
  const quadranteDe = (d, p) => m?.quadrantes.find((q) => q.desempenho === d && q.potencial === p);
  const pessoasEm = (d, p) => (m?.itens || []).filter((i) => !i.pendente && i.desempenho === d && i.potencial === p);
  const pendentes = (m?.itens || []).filter((i) => i.pendente);

  function abrirRegistro(item) {
    if (!podeRegistrar(item)) return;
    acao.setErro(null);
    setRegistro({ ...item, desempenho: item.desempenho ?? item.desempenhoSugerido, potencial: item.potencial,
      observacoes: item.observacoes || '' });
  }

  async function salvar(e) {
    e.preventDefault();
    try {
      await acao.executar(() => api.put('/nine-box', { cicloId: Number(cicloId), colaboradorId: registro.colaboradorId,
        desempenho: registro.desempenho, potencial: registro.potencial, observacoes: registro.observacoes || null }));
      setRegistro(null);
      matriz.recarregar();
    } catch {
      /* exibido */
    }
  }

  const escolher = (chave) => (v) => setParams((p) => { if (v) p.set(chave, v); else p.delete(chave); return p; });
  const previa = registro?.desempenho && registro?.potencial ? quadranteDe(registro.desempenho, registro.potencial) : null;

  return (
    <Pagina titulo="Matriz Nine Box"
      subtitulo="Desempenho × potencial como apoio à análise. O sistema não toma decisões automáticas de promoção, desligamento ou PDI.">
      <div className="filtros">
        <Campo rotulo="Ciclo">
          <Seletor valor={cicloId} onChange={escolher('ciclo')}
            opcoes={disponiveis.map((c) => ({ valor: c.id, rotulo: `${c.nome} — ${ROTULOS.statusCiclo[c.status]}` }))} />
        </Campo>
        {isRH && (
          <Campo rotulo="Departamento">
            <Seletor valor={dept} onChange={escolher('departamento')} vazio="Todos"
              opcoes={(depts.data || []).map((d) => ({ valor: d.id, rotulo: d.nome }))} />
          </Campo>
        )}
      </div>
      <Alerta erro={matriz.erro} />
      {disponiveis.length === 0 && <Vazio>Nenhum ciclo liberado.</Vazio>}
      {m && (isGestor || temEquipe) && !liberado && (
        <Alerta tipo="aviso">A Nine Box é registrada depois que o RH libera os resultados do ciclo.</Alerta>
      )}
      {m && liberado && temEquipe && (
        <Alerta tipo="info">Clique em um colaborador da sua equipe para registrar ou alterar a classificação.</Alerta>
      )}
      {m && (
        <div className="grade grade-ninebox">
          <Cartao>
            <div className="ninebox">
              <div className="ninebox-eixo-y">Potencial →</div>
              {['ALTO', 'MEDIO', 'BAIXO'].map((p) => ['BAIXO', 'MEDIO', 'ALTO'].map((d) => {
                const q = quadranteDe(d, p);
                return (
                  <div key={d + p} className={`quadro ${cor(d, p)}`} role="button" tabIndex={0}
                    onClick={() => setQuadrante(q)} onKeyDown={(e) => e.key === 'Enter' && setQuadrante(q)}>
                    <div className="quadro-titulo"><span>{q?.rotulo}</span><span>{pessoasEm(d, p).length}</span></div>
                    <div className="quadro-eixos">Desempenho {ROTULOS.nivel[d].toLowerCase()} · Potencial {ROTULOS.nivel[p].toLowerCase()}</div>
                    <div className="quadro-pessoas">
                      {pessoasEm(d, p).map((i) => (
                        <span key={i.colaboradorId} className="pessoa-chip" title={`${i.cargo} · ${i.departamento}`}
                          onClick={(e) => { e.stopPropagation(); if (podeRegistrar(i)) abrirRegistro(i); else setQuadrante(q); }}>
                          {i.colaboradorNome}
                        </span>
                      ))}
                    </div>
                  </div>
                );
              }))}
              <div className="ninebox-eixo-x">Desempenho →</div>
            </div>
          </Cartao>
          <Cartao titulo={`Pendentes (${pendentes.length})`}>
            <p className="texto-2 pequeno">Sem desempenho ou potencial registrado: não aparecem em nenhum quadrante.</p>
            {pendentes.length === 0 ? <p className="texto-2">Nenhum.</p> : (
              <table>
                <tbody>
                  {pendentes.map((i) => (
                    <tr key={i.colaboradorId}>
                      <td>
                        <strong>{i.colaboradorNome}</strong>
                        <div className="texto-2 pequeno">
                          Média gestor: {fmtNum(i.mediaGestor)}
                          {i.desempenhoSugerido && ` · sugerido: ${ROTULOS.nivel[i.desempenhoSugerido]}`}
                        </div>
                      </td>
                      <td className="acoes">{podeRegistrar(i) && <button className="btn-texto" onClick={() => abrirRegistro(i)}>Registrar</button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </Cartao>
        </div>
      )}

      <Modal titulo={quadrante ? `${quadrante.rotulo}` : ''} aberto={quadrante !== null} onFechar={() => setQuadrante(null)}>
        {quadrante && (
          <div className="form">
            <p className="texto-2">Desempenho {ROTULOS.nivel[quadrante.desempenho].toLowerCase()} · Potencial {ROTULOS.nivel[quadrante.potencial].toLowerCase()}</p>
            <p><strong>{quadrante.descricao}</strong></p>
            <div>
              <span className="campo-rotulo">Plano de ação sugerido</span>
              <ul>{quadrante.acoes.map((a) => <li key={a}>{a}</li>)}</ul>
            </div>
            <Alerta tipo="aviso">Sugestões para apoiar o feedback e a proposta de PDI. A decisão é sempre do gestor e do RH.</Alerta>
          </div>
        )}
      </Modal>

      <Modal titulo={registro ? `Nine Box — ${registro.colaboradorNome}` : ''} aberto={registro !== null} onFechar={() => setRegistro(null)}>
        {registro && (
          <form className="form" onSubmit={salvar}>
            <Alerta erro={acao.erro} />
            <p className="texto-2">
              Média da avaliação do gestor: <strong>{fmtNum(registro.mediaGestor)}</strong>
              {registro.desempenhoSugerido && <> · desempenho sugerido: <strong>{ROTULOS.nivel[registro.desempenhoSugerido]}</strong></>}
            </p>
            <div className="grade grade-2">
              <Campo rotulo="Desempenho" dica="Você pode confirmar ou alterar a sugestão">
                <Seletor valor={registro.desempenho} onChange={(v) => setRegistro({ ...registro, desempenho: v })} vazio="Não informado" opcoes={opcoesNivel} />
              </Campo>
              <Campo rotulo="Potencial">
                <Seletor valor={registro.potencial} onChange={(v) => setRegistro({ ...registro, potencial: v })} vazio="Não informado" opcoes={opcoesNivel} />
              </Campo>
            </div>
            <Campo rotulo="Observações (fatores de risco, contexto)">
              <textarea value={registro.observacoes} onChange={(e) => setRegistro({ ...registro, observacoes: e.target.value })} />
            </Campo>
            {previa ? (
              <Alerta tipo="info"><strong>{previa.rotulo}:</strong> {previa.descricao}</Alerta>
            ) : <Alerta tipo="aviso">Sem os dois eixos a classificação fica pendente.</Alerta>}
            <div className="form-acoes">
              <button type="button" onClick={() => setRegistro(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        )}
      </Modal>
    </Pagina>
  );
}
