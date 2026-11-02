import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { useApi } from '../hooks.js';
import { Alerta, Campo, Cartao, Carregando, Pagina, Seletor, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtNum } from '../format.js';

function Tabela({ titulo, linhas, colaboradores }) {
  const medias = linhas.map((l) => l.mediaGestor).filter((m) => m != null);
  const max = Math.max(...medias);
  const min = Math.min(...medias);
  return (
    <Cartao titulo={titulo}>
      <div className="tabela-wrap">
        <table>
          <thead>
            <tr>
              <th>Competência</th>
              {colaboradores.map((c) => <th key={c.id} className="num">{c.nome.split(' ')[0]}</th>)}
              <th className="num">Média gestor</th>
              <th className="num">Média autoavaliação</th>
            </tr>
          </thead>
          <tbody>
            {linhas.map((l) => (
              <tr key={l.competenciaId}
                className={medias.length > 1 && l.mediaGestor === max ? 'destaque-bom' : medias.length > 1 && l.mediaGestor === min ? 'destaque-atencao' : ''}>
                <td>{l.competencia}</td>
                {l.notasGestor.map((n, i) => <td key={i} className="num">{n ?? '—'}</td>)}
                <td className="num"><strong>{fmtNum(l.mediaGestor)}</strong></td>
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
      subtitulo="Competência × colaborador avaliado, com a média de cada competência. Verde: melhor resultado; amarelo: exige atenção.">
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
          <Tabela titulo="Competências comportamentais" linhas={m.comportamentais} colaboradores={m.colaboradores} />
          <Tabela titulo="Competências técnicas" linhas={m.tecnicas} colaboradores={m.colaboradores} />
          <p className="texto-2 pequeno">Médias calculadas apenas com notas válidas de avaliações concluídas; notas ausentes não contam como zero.</p>
        </>
      ))}
    </Pagina>
  );
}
