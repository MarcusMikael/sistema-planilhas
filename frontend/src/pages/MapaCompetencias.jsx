import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { useApi } from '../hooks.js';
import { Alerta, Campo, Cartao, Carregando, Pagina, Seletor, Vazio } from '../components/ui.jsx';
import { CelulaCalor, LegendaCalor } from '../components/graficos.jsx';
import { ROTULOS, fmtNum } from '../format.js';

/** Competência × colaborador como mapa de calor: quanto mais escuro o verde, maior a nota do gestor. */
function Tabela({ titulo, linhas, colaboradores }) {
  return (
    <Cartao titulo={titulo}>
      <div className="tabela-wrap">
        <table>
          <thead>
            <tr>
              <th>Competência</th>
              {colaboradores.map((c) => <th key={c.id} className="num" title={c.nome}>{c.nome.split(' ')[0]}</th>)}
              <th className="num">Média gestor</th>
              <th className="num">Média autoavaliação</th>
            </tr>
          </thead>
          <tbody>
            {linhas.map((l) => (
              <tr key={l.competenciaId}>
                <td>{l.competencia}</td>
                {l.notasGestor.map((n, i) => (
                  <CelulaCalor key={colaboradores[i].id} valor={n}
                    titulo={`${colaboradores[i].nome} · ${l.competencia}: ${n ?? 'sem nota'}`} />
                ))}
                <CelulaCalor valor={l.mediaGestor} forte titulo={`Média do gestor em ${l.competencia}: ${fmtNum(l.mediaGestor)}`} />
                <td className="num">{fmtNum(l.mediaAutoavaliacao)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Cartao>
  );
}

export default function MapaCompetencias() {
  const { isRH } = useAuth();
  const [params, setParams] = useSearchParams();
  const ciclos = useApi('/ciclos');
  const disponiveis = (ciclos.data || []).filter((c) => (isRH ? c.status !== 'RASCUNHO' : c.status === 'RESULTADOS_LIBERADOS'));
  const cicloId = params.get('ciclo') || disponiveis[0]?.id;
  const mapa = useApi(cicloId ? `/ciclos/${cicloId}/mapa-competencias` : null);

  if (ciclos.carregando) return <Carregando />;
  const m = mapa.data;
  return (
    <Pagina titulo="Mapa de competências"
      subtitulo="Notas do gestor por competência e colaborador. Quanto mais escuro o verde, maior a nota; células claras mostram as lacunas da equipe.">
      <div className="filtros">
        <Campo rotulo="Ciclo">
          <Seletor valor={cicloId} onChange={(v) => setParams({ ciclo: v })}
            opcoes={disponiveis.map((c) => ({ valor: c.id, rotulo: `${c.nome} — ${ROTULOS.statusCiclo[c.status]}` }))} />
        </Campo>
      </div>
      <Alerta erro={mapa.erro} />
      {disponiveis.length === 0 && <Vazio>Nenhum ciclo com resultados disponíveis.</Vazio>}
      {mapa.carregando && <Carregando />}
      {m && (m.colaboradores.length === 0 ? <Vazio>Nenhuma avaliação do gestor concluída neste ciclo.</Vazio> : (
        <>
          <LegendaCalor />
          <Tabela titulo="Competências comportamentais" linhas={m.comportamentais} colaboradores={m.colaboradores} />
          <Tabela titulo="Competências técnicas" linhas={m.tecnicas} colaboradores={m.colaboradores} />
          <p className="texto-2 pequeno">Médias calculadas apenas com notas válidas de avaliações concluídas; notas ausentes não contam como zero.</p>
        </>
      ))}
    </Pagina>
  );
}
