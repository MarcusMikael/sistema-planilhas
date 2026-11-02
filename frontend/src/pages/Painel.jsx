import { Link } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { useApi } from '../hooks.js';
import { Alerta, Badge, Cartao, Carregando, Indicador, Pagina, Progresso, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtData, fmtNum } from '../format.js';

function pct(a, b) {
  return b ? `${Math.round((a / b) * 100)}%` : '—';
}

export default function Painel() {
  const { usuario, isRH, isGestor } = useAuth();
  const ind = useApi('/indicadores');
  const pend = useApi(usuario.colaboradorId ? '/avaliacoes/pendentes' : null);
  const pdis = useApi(!isRH && !isGestor ? '/pdis' : null);
  const clima = useApi('/pesquisas-clima');

  if (ind.carregando) return <Carregando />;
  const d = ind.data;
  const minhasPend = (pend.data || []).filter((p) => p.status !== 'CONCLUIDA');
  const climaAberto = (clima.data || []).filter((p) => p.abertaParaResposta && p.jaRespondeu === false);

  return (
    <Pagina
      titulo={`Olá, ${usuario.nome.split(' ')[0]}`}
      subtitulo={d?.escopo === 'ORGANIZACAO' ? 'Visão consolidada da organização'
        : d?.escopo === 'EQUIPE' ? 'Visão da sua equipe' : 'Suas atividades de desenvolvimento'}
    >
      <Alerta erro={ind.erro} />
      {d && (isRH || isGestor) && (
        <>
          <div className="grade grade-4">
            <Indicador rotulo={isRH ? 'Colaboradores ativos' : 'Pessoas na equipe'} valor={d.totalColaboradores} />
            <Indicador rotulo="Autoavaliações concluídas" valor={pct(d.autoavaliacoesConcluidas, d.avaliacoesEsperadas)}
              detalhe={`${d.autoavaliacoesConcluidas} de ${d.avaliacoesEsperadas} · ${d.cicloAtual?.nome ?? 'sem ciclo'}`} />
            <Indicador rotulo="Avaliações do gestor concluídas" valor={pct(d.avaliacoesGestorConcluidas, d.avaliacoesEsperadas)}
              detalhe={`${d.avaliacoesGestorConcluidas} de ${d.avaliacoesEsperadas}`} />
            <Indicador rotulo="Média geral (gestor)" valor={fmtNum(d.mediaGeralGestor)}
              detalhe={d.mediaGeralGestor == null ? 'Disponível após a liberação' : 'Escala de 0 a 5'} />
          </div>
          <div className="grade grade-3">
            <Cartao titulo="Ciclo atual">
              {d.cicloAtual ? (
                <div className="form">
                  <div className="linha"><strong>{d.cicloAtual.nome}</strong>
                    <Badge status={d.cicloAtual.status} grupo="statusCiclo" /></div>
                  <Link to={isRH ? `/ciclos/${d.cicloAtual.id}` : '/avaliacoes'}>Abrir ciclo →</Link>
                </div>
              ) : <Vazio>Nenhum ciclo liberado.</Vazio>}
            </Cartao>
            <Cartao titulo="Nine Box do ciclo">
              {Object.keys(d.distribuicaoNineBox).length === 0 ? (
                <p className="texto-2">Nenhuma classificação registrada.</p>
              ) : (
                <div className="barras">
                  {Object.entries(d.distribuicaoNineBox).map(([rot, n]) => (
                    <div className="barra-linha" key={rot}>
                      <span>{rot}</span>
                      <div className="barra-fundo"><div className="barra-cheia"
                        style={{ width: `${(n / d.totalColaboradores) * 100}%` }} /></div>
                      <span className="num">{n}</span>
                    </div>
                  ))}
                </div>
              )}
              <p className="texto-2 pequeno" style={{ marginTop: 8 }}>{d.pendentesNineBox} pendente(s) de classificação</p>
            </Cartao>
            <Cartao titulo="PDIs">
              <div className="grade grade-2">
                <Indicador rotulo="PDIs ativos" valor={d.pdisAtivos} />
                <Indicador rotulo="Ações atrasadas" valor={d.acoesAtrasadas} />
              </div>
              <p className="texto-2 pequeno" style={{ marginTop: 8 }}>
                {Object.entries(d.acoesPorStatus).map(([s, n]) => `${ROTULOS.statusAcao[s]}: ${n}`).join(' · ')}
              </p>
            </Cartao>
          </div>
          {isRH && d.pesquisaAtual && (
            <Cartao titulo="Pesquisa de clima em andamento"
              acoes={<Link to={`/clima/${d.pesquisaAtual.id}/resultados`}>Ver resultados agregados →</Link>}>
              <div className="linha">
                <strong>{d.pesquisaAtual.titulo}</strong>
                <span className="texto-2">{d.pesquisaAtual.participacoes} de {d.pesquisaAtual.elegiveis} responderam</span>
                <div className="espaco" />
                <div style={{ width: 240 }}>
                  <Progresso valor={Math.round((d.pesquisaAtual.participacoes / Math.max(1, d.pesquisaAtual.elegiveis)) * 100)} />
                </div>
              </div>
            </Cartao>
          )}
        </>
      )}

      {usuario.colaboradorId && (
        <Cartao titulo="Suas pendências">
          {minhasPend.length === 0 && climaAberto.length === 0 ? (
            <Vazio>Você não tem pendências no momento.</Vazio>
          ) : (
            <div className="tabela-wrap">
              <table>
                <tbody>
                  {minhasPend.map((p) => (
                    <tr key={`${p.cicloId}-${p.colaboradorId}-${p.tipo}`}>
                      <td>{ROTULOS.tipoAvaliacao[p.tipo]}{p.tipo === 'GESTOR' && ` · ${p.colaboradorNome}`}</td>
                      <td className="texto-2">{p.cicloNome} · até {fmtData(p.cicloDataFim)}</td>
                      <td><Badge status={p.status || 'PENDENTE'}>{p.status ? ROTULOS.statusAvaliacao[p.status] : 'Não iniciada'}</Badge></td>
                      <td className="acoes"><Link to="/avaliacoes">Avaliar →</Link></td>
                    </tr>
                  ))}
                  {climaAberto.map((p) => (
                    <tr key={`clima-${p.id}`}>
                      <td>Pesquisa de clima</td>
                      <td className="texto-2">{p.titulo} · até {fmtData(p.dataFim)}</td>
                      <td><Badge status="PENDENTE">Não respondida</Badge></td>
                      <td className="acoes"><Link to={`/clima/${p.id}/responder`}>Responder →</Link></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Cartao>
      )}

      {pdis.data && (
        <Cartao titulo="Meu PDI" acoes={<Link to="/pdis">Ver todos →</Link>}>
          {pdis.data.filter((p) => p.status === 'ATIVO').length === 0 ? (
            <Vazio>Nenhum PDI ativo.</Vazio>
          ) : pdis.data.filter((p) => p.status === 'ATIVO').map((p) => (
            <div key={p.id} className="linha" style={{ marginBottom: 8 }}>
              <Link to={`/pdis/${p.id}`}><strong>{p.objetivo}</strong></Link>
              <div className="espaco" />
              <div style={{ width: 200 }}><Progresso valor={p.progressoGeral} /></div>
            </div>
          ))}
        </Cartao>
      )}
    </Pagina>
  );
}
