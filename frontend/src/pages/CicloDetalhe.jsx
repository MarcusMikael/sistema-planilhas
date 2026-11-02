import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Pagina, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtData } from '../format.js';

const POR_TIPO = 5;

export default function CicloDetalhe() {
  const { id } = useParams();
  const navegar = useNavigate();
  const ciclo = useApi(`/ciclos/${id}`);
  const catalogo = useApi('/competencias');
  const avaliacoes = useApi(`/avaliacoes?cicloId=${id}`);
  const colaboradores = useApi('/colaboradores');
  const [selecao, setSelecao] = useState(new Set());
  const [datas, setDatas] = useState(null);
  const [aviso, setAviso] = useState(null);
  const acao = useAcao();

  useEffect(() => {
    if (ciclo.data) {
      setSelecao(new Set(ciclo.data.competencias.map((c) => c.competencia.id)));
      setDatas({ nome: ciclo.data.nome, dataInicio: ciclo.data.dataInicio, dataFim: ciclo.data.dataFim });
    }
  }, [ciclo.data]);

  if (ciclo.carregando || catalogo.carregando) return <Carregando />;
  if (ciclo.erro) return <Alerta erro={ciclo.erro} />;
  const c = ciclo.data;
  const rascunho = c.status === 'RASCUNHO';
  const contagem = (tipo) => (catalogo.data || []).filter((x) => x.tipo === tipo && selecao.has(x.id)).length;
  const qComp = contagem('COMPORTAMENTAL');
  const qTec = contagem('TECNICA');

  function alternar(comp) {
    const nova = new Set(selecao);
    if (nova.has(comp.id)) nova.delete(comp.id);
    else if (contagem(comp.tipo) >= POR_TIPO) {
      setAviso(`Já há ${POR_TIPO} competências do tipo ${ROTULOS.tipoCompetencia[comp.tipo].toLowerCase()} selecionadas.`);
      return;
    } else nova.add(comp.id);
    setAviso(null);
    setSelecao(nova);
  }

  async function executar(fn, msg) {
    try {
      const r = await acao.executar(fn);
      if (r) ciclo.setData(r);
      setAviso(msg);
      avaliacoes.recarregar();
    } catch {
      setAviso(null);
    }
  }

  const salvarSelecao = () => executar(() => api.put(`/ciclos/${id}/competencias`, { competenciaIds: [...selecao] }), 'Seleção salva.');
  const salvarDatas = (e) => {
    e.preventDefault();
    executar(() => api.put(`/ciclos/${id}`, datas), 'Dados do ciclo salvos.');
  };

  // Acompanhamento: quem já concluiu cada tipo de avaliação
  const ativos = (colaboradores.data || []).filter((x) => x.ativo);
  const status = (colabId, tipo) => (avaliacoes.data || []).find((a) => a.colaboradorId === colabId && a.tipo === tipo);

  return (
    <Pagina titulo={c.nome} subtitulo={`${fmtData(c.dataInicio)} a ${fmtData(c.dataFim)}`}
      acoes={<>
        <Badge status={c.status} grupo="statusCiclo" />
        {rascunho && (
          <button className="btn-primario" disabled={acao.enviando || qComp !== POR_TIPO || qTec !== POR_TIPO}
            title={qComp !== POR_TIPO || qTec !== POR_TIPO ? 'Selecione e salve 5 + 5 competências' : ''}
            onClick={async () => {
              if (window.confirm('Liberar o ciclo? As competências não poderão mais ser alteradas.')) {
                await executar(() => api.put(`/ciclos/${id}/competencias`, { competenciaIds: [...selecao] }));
                executar(() => api.post(`/ciclos/${id}/liberar`), 'Ciclo liberado: colaboradores já podem fazer a autoavaliação.');
              }
            }}>Liberar ciclo</button>
        )}
        {c.status === 'LIBERADO' && (
          <button className="btn-primario" disabled={acao.enviando} onClick={() => {
            if (window.confirm('Liberar os resultados? Gestores e colaboradores passarão a consultá-los e o ciclo será fechado para avaliações.')) {
              executar(() => api.post(`/ciclos/${id}/liberar-resultados`), 'Resultados liberados.');
            }
          }}>Liberar resultados</button>
        )}
        {c.status !== 'RASCUNHO' && <Link className="btn" to={`/mapa-competencias?ciclo=${id}`}>Mapa de competências</Link>}
        {rascunho && (
          <button className="btn-perigo" onClick={async () => {
            if (window.confirm('Excluir este ciclo?')) {
              try { await acao.executar(() => api.del(`/ciclos/${id}`)); navegar('/ciclos'); } catch { /* exibido */ }
            }
          }}>Excluir</button>
        )}
      </>}>
      <Alerta erro={acao.erro} />
      {aviso && <Alerta tipo="info">{aviso}</Alerta>}

      {datas && c.status !== 'RESULTADOS_LIBERADOS' && (
        <Cartao titulo="Dados do ciclo">
          <form className="filtros" onSubmit={salvarDatas}>
            <Campo rotulo="Nome"><input value={datas.nome} onChange={(e) => setDatas({ ...datas, nome: e.target.value })} required /></Campo>
            <Campo rotulo="Início"><input type="date" value={datas.dataInicio} onChange={(e) => setDatas({ ...datas, dataInicio: e.target.value })} required /></Campo>
            <Campo rotulo="Término"><input type="date" value={datas.dataFim} onChange={(e) => setDatas({ ...datas, dataFim: e.target.value })} required /></Campo>
            <button disabled={acao.enviando}>Salvar</button>
          </form>
        </Cartao>
      )}

      <Cartao titulo="Competências do ciclo" acoes={rascunho && (
        <div className="linha">
          <span className={qComp === POR_TIPO ? 'badge badge-ok' : 'badge badge-aviso'}>Comportamentais {qComp}/{POR_TIPO}</span>
          <span className={qTec === POR_TIPO ? 'badge badge-ok' : 'badge badge-aviso'}>Técnicas {qTec}/{POR_TIPO}</span>
          <button className="btn-primario" onClick={salvarSelecao} disabled={acao.enviando}>Salvar seleção</button>
        </div>
      )}>
        <div className="grade grade-2">
          {['COMPORTAMENTAL', 'TECNICA'].map((tipo) => (
            <div key={tipo}>
              <h3 style={{ marginBottom: 8 }}>{ROTULOS.tipoCompetenciaPlural[tipo]}</h3>
              <table>
                <thead><tr><th>Competência</th><th>Selecionar</th></tr></thead>
                <tbody>
                  {(catalogo.data || []).filter((x) => x.tipo === tipo && (x.ativo || selecao.has(x.id)))
                    .filter((x) => rascunho || selecao.has(x.id))
                    .map((x) => (
                      <tr key={x.id}>
                        <td><strong>{x.nome}</strong><div className="texto-2 pequeno">{x.descricao}</div></td>
                        <td>
                          {rascunho ? (
                            <label className="check"><input type="checkbox" checked={selecao.has(x.id)} onChange={() => alternar(x)} />
                              {selecao.has(x.id) ? 'Sim' : 'Não'}</label>
                          ) : <Badge status="ATIVO">Sim</Badge>}
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          ))}
        </div>
      </Cartao>

      {!rascunho && (
        <Cartao titulo="Acompanhamento das avaliações">
          {ativos.length === 0 ? <Vazio>Sem colaboradores.</Vazio> : (
            <div className="tabela-wrap">
              <table>
                <thead><tr><th>Colaborador</th><th>Gestor</th><th>Autoavaliação</th><th>Avaliação do gestor</th><th /></tr></thead>
                <tbody>
                  {ativos.map((p) => {
                    const auto = status(p.id, 'AUTOAVALIACAO');
                    const gest = status(p.id, 'GESTOR');
                    return (
                      <tr key={p.id}>
                        <td>{p.nome}</td>
                        <td>{p.gestorNome ?? '—'}</td>
                        <td>{auto ? <Badge status={auto.status} grupo="statusAvaliacao" /> : <Badge status="PENDENTE">Não iniciada</Badge>}</td>
                        <td>{gest ? <Badge status={gest.status} grupo="statusAvaliacao" /> : <Badge status="PENDENTE">Não iniciada</Badge>}</td>
                        <td className="acoes"><Link to={`/resultados?ciclo=${id}&colaborador=${p.id}`}>Resultado →</Link></td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </Cartao>
      )}
    </Pagina>
  );
}
