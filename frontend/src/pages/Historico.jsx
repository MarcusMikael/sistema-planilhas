import { Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Pagina, Progresso, Seletor, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtData, fmtNum } from '../format.js';

export default function Historico() {
  const { usuario, isRH, isGestor } = useAuth();
  const [params, setParams] = useSearchParams();
  const pessoas = useApi(isRH || isGestor ? '/colaboradores' : null);
  const colaboradorId = params.get('colaborador') || usuario.colaboradorId;
  const hist = useApi(colaboradorId ? `/colaboradores/${colaboradorId}/historico` : null);

  if (!colaboradorId && !(isRH || isGestor)) {
    return <Pagina titulo="Histórico"><Vazio>Sua conta não está vinculada a um colaborador.</Vazio></Pagina>;
  }
  const h = hist.data;

  return (
    <Pagina titulo="Histórico" subtitulo="Resultados por ciclo, classificação na Nine Box e PDIs">
      {(isRH || isGestor) && (
        <div className="filtros">
          <Campo rotulo="Colaborador">
            <Seletor valor={colaboradorId} onChange={(v) => setParams(v ? { colaborador: v } : {})} vazio="Selecione"
              opcoes={(pessoas.data || []).map((p) => ({ valor: p.id, rotulo: p.nome }))} />
          </Campo>
        </div>
      )}
      <Alerta erro={hist.erro} />
      {hist.carregando && <Carregando />}
      {h && (
        <>
          <Cartao>
            <div className="linha">
              <strong>{h.colaborador.nome}</strong>
              <span className="texto-2">{h.colaborador.cargoNome} · {h.colaborador.departamentoNome} · admissão {fmtData(h.colaborador.dataAdmissao)}</span>
            </div>
          </Cartao>
          <Cartao titulo="Ciclos de avaliação">
            {h.ciclos.length === 0 ? <Vazio>Nenhum ciclo liberado.</Vazio> : (
              <div className="tabela-wrap">
                <table>
                  <thead><tr><th>Ciclo</th><th>Situação</th><th className="num">Gestor (geral)</th><th className="num">Autoavaliação (geral)</th><th>Nine Box</th><th /></tr></thead>
                  <tbody>
                    {h.ciclos.map((c) => (
                      <tr key={c.cicloId}>
                        <td><strong>{c.cicloNome}</strong><div className="texto-2 pequeno">início {fmtData(c.dataInicio)}</div></td>
                        <td><Badge status={c.status} grupo="statusCiclo" /></td>
                        <td className="num">{c.resultadosDisponiveis ? fmtNum(c.gestor?.geral) : 'não liberado'}</td>
                        <td className="num">{c.autoavaliacao ? (c.autoavaliacao.geral != null ? fmtNum(c.autoavaliacao.geral)
                          : ROTULOS.statusAvaliacao[c.autoavaliacao.status]) : '—'}</td>
                        <td>{c.quadrante ? `${c.quadrante} (${ROTULOS.nivel[c.desempenho]} × ${ROTULOS.nivel[c.potencial]})` : '—'}</td>
                        <td className="acoes">{c.resultadosDisponiveis && (
                          <Link to={`/resultados?ciclo=${c.cicloId}&colaborador=${h.colaborador.id}`}>Detalhes →</Link>
                        )}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Cartao>
          <Cartao titulo="PDIs">
            {h.pdis.length === 0 ? <Vazio>Nenhum PDI.</Vazio> : (
              <table>
                <tbody>
                  {h.pdis.map((p) => (
                    <tr key={p.id}>
                      <td><Link to={`/pdis/${p.id}`}><strong>{p.objetivo}</strong></Link><div className="texto-2 pequeno">criado em {fmtData(p.criadoEm)} · {p.totalAcoes} ações</div></td>
                      <td><Badge status={p.status} grupo="statusPDI" /></td>
                      <td style={{ width: 180 }}><Progresso valor={p.progressoGeral} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </Cartao>
        </>
      )}
    </Pagina>
  );
}
