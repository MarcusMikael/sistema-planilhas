import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Indicador, Pagina, Seletor, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtNum } from '../format.js';

function Medias({ titulo, m }) {
  if (!m) return <Cartao titulo={titulo}><p className="texto-2">Não realizada.</p></Cartao>;
  if (m.status !== 'CONCLUIDA') return <Cartao titulo={titulo}><Badge status={m.status} grupo="statusAvaliacao" /></Cartao>;
  return (
    <Cartao titulo={titulo}>
      <div className="grade grade-3">
        <Indicador rotulo="Comportamental" valor={fmtNum(m.comportamental)} />
        <Indicador rotulo="Técnica" valor={fmtNum(m.tecnica)} />
        <Indicador rotulo="Geral" valor={fmtNum(m.geral)} />
      </div>
    </Cartao>
  );
}

export default function Resultados() {
  const { usuario, isRH, isGestor } = useAuth();
  const [params, setParams] = useSearchParams();
  const ciclos = useApi('/ciclos');
  const pessoas = useApi(isRH || isGestor ? '/colaboradores' : null);

  const disponiveis = (ciclos.data || []).filter((c) => (isRH ? c.status !== 'RASCUNHO' : c.status === 'RESULTADOS_LIBERADOS'));
  const cicloId = params.get('ciclo') || disponiveis[0]?.id;
  const colaboradorId = params.get('colaborador') || (isRH || isGestor ? null : usuario.colaboradorId);
  const res = useApi(cicloId && colaboradorId ? `/ciclos/${cicloId}/resultados/${colaboradorId}` : null);

  useEffect(() => {
    if (!params.get('colaborador') && (isRH || isGestor) && pessoas.data?.length) {
      const primeiro = pessoas.data.find((p) => p.id !== usuario.colaboradorId) || pessoas.data[0];
      setParams((p) => { p.set('colaborador', primeiro.id); return p; }, { replace: true });
    }
  }, [pessoas.data]); // eslint-disable-line react-hooks/exhaustive-deps

  const escolher = (chave) => (v) => setParams((p) => { if (v) p.set(chave, v); else p.delete(chave); return p; });
  if (ciclos.carregando) return <Carregando />;
  const r = res.data;

  return (
    <Pagina titulo={isRH || isGestor ? 'Resultados de avaliação' : 'Meus resultados'}
      subtitulo="O resultado oficial é a avaliação do gestor; a autoavaliação é exibida para comparação.">
      <div className="filtros">
        <Campo rotulo="Ciclo">
          <Seletor valor={cicloId} onChange={escolher('ciclo')}
            opcoes={disponiveis.map((c) => ({ valor: c.id, rotulo: `${c.nome} — ${ROTULOS.statusCiclo[c.status]}` }))} />
        </Campo>
        {(isRH || isGestor) && (
          <Campo rotulo="Colaborador">
            <Seletor valor={colaboradorId} onChange={escolher('colaborador')}
              opcoes={(pessoas.data || []).map((p) => ({ valor: p.id, rotulo: p.nome }))} />
          </Campo>
        )}
      </div>
      {disponiveis.length === 0 ? <Vazio>Nenhum ciclo com resultados liberados.</Vazio> : (
        <>
          {isRH && disponiveis.find((c) => String(c.id) === String(cicloId))?.status === 'LIBERADO' && (
            <Alerta tipo="aviso">Resultados ainda não liberados: apenas o RH pode vê-los neste momento.</Alerta>
          )}
          <Alerta erro={res.erro} />
          {res.carregando && <Carregando />}
          {r && (
            <>
              <Cartao>
                <div className="linha">
                  <strong>{r.colaboradorNome}</strong>
                  <span className="texto-2">{r.cargo} · {r.departamento}</span>
                </div>
              </Cartao>
              <div className="grade grade-2">
                <Medias titulo="Avaliação do gestor" m={r.gestor} />
                <Medias titulo="Autoavaliação" m={r.autoavaliacao} />
              </div>
              <Cartao titulo="Comparação por competência">
                <div className="tabela-wrap">
                  <table>
                    <thead><tr><th>Competência</th><th>Tipo</th><th className="num">Gestor</th><th className="num">Autoavaliação</th><th className="num">Diferença</th></tr></thead>
                    <tbody>
                      {r.itens.map((i) => {
                        const dif = i.notaGestor != null && i.notaAutoavaliacao != null ? i.notaAutoavaliacao - i.notaGestor : null;
                        return (
                          <tr key={i.competencia}>
                            <td>{i.competencia}</td>
                            <td className="texto-2">{ROTULOS.tipoCompetencia[i.tipo]}</td>
                            <td className="num">{i.notaGestor ?? '—'}</td>
                            <td className="num">{i.notaAutoavaliacao ?? '—'}</td>
                            <td className="num">{dif == null ? '—' : dif > 0 ? `+${dif}` : dif}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </Cartao>
            </>
          )}
        </>
      )}
    </Pagina>
  );
}
