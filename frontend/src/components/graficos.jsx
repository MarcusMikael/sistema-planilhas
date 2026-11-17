import { useState } from 'react';
import { ROTULOS, fmtNum } from '../format.js';

const ESCALA_MAX = 5;
const MARCAS = [0, 1, 2, 3, 4, 5];

/**
 * Barras horizontais agrupadas: nota do gestor × autoavaliação por competência (escala 0 a 5).
 * Cores validadas para daltonismo (esmeralda × índigo, ΔE 23,6). A tabela da página continua sendo a
 * visão em texto dos mesmos dados; aqui não há número em cada barra, só no tooltip (hover ou foco).
 */
export function GraficoComparacao({ itens }) {
  const [ativo, setAtivo] = useState(null);
  const grupos = ['COMPORTAMENTAL', 'TECNICA']
    .map((tipo) => ({ tipo, itens: itens.filter((i) => i.tipo === tipo) }))
    .filter((g) => g.itens.length > 0);

  return (
    <div className="gc" onMouseLeave={() => setAtivo(null)}>
      <div className="gc-legenda" aria-hidden="true">
        <span><i className="gc-amostra gc-gestor" /> Gestor (resultado oficial)</span>
        <span><i className="gc-amostra gc-auto" /> Autoavaliação</span>
      </div>
      {grupos.map((g) => (
        <div key={g.tipo} className="gc-grupo">
          <div className="gc-grupo-titulo">{ROTULOS.tipoCompetenciaPlural[g.tipo]}</div>
          {g.itens.map((i) => {
            const chave = `${g.tipo}-${i.competencia}`;
            const dif = i.notaGestor != null && i.notaAutoavaliacao != null ? i.notaAutoavaliacao - i.notaGestor : null;
            const descricao = `${i.competencia}: gestor ${i.notaGestor ?? 'sem nota'}, autoavaliação ${i.notaAutoavaliacao ?? 'sem nota'}`;
            return (
              <div key={chave} className={`gc-linha ${ativo === chave ? 'ativa' : ''}`} tabIndex={0} aria-label={descricao}
                onMouseEnter={() => setAtivo(chave)} onFocus={() => setAtivo(chave)} onBlur={() => setAtivo(null)}>
                <span className="gc-rotulo" title={i.competencia}>{i.competencia}</span>
                <div className="gc-area">
                  <Barra valor={i.notaGestor} classe="gc-gestor" />
                  <Barra valor={i.notaAutoavaliacao} classe="gc-auto" />
                  {ativo === chave && (
                    <div className="gc-tooltip" role="presentation">
                      <strong>{i.competencia}</strong>
                      <span><i className="gc-amostra gc-gestor" /> Gestor <b>{i.notaGestor ?? '—'}</b></span>
                      <span><i className="gc-amostra gc-auto" /> Autoavaliação <b>{i.notaAutoavaliacao ?? '—'}</b></span>
                      {dif != null && <span className="texto-2">Diferença {dif > 0 ? `+${dif}` : dif}</span>}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ))}
      <div className="gc-linha gc-eixo" aria-hidden="true">
        <span />
        <div className="gc-area">
          {MARCAS.map((m) => <span key={m} style={{ left: `${(m / ESCALA_MAX) * 100}%` }}>{m}</span>)}
        </div>
      </div>
    </div>
  );
}

function Barra({ valor, classe }) {
  if (valor == null) return <div className="gc-barra gc-vazia" />;
  // Nota 0 vira um traço mínimo, para não se confundir com "sem nota"
  return <div className={`gc-barra ${classe}`} style={{ width: valor === 0 ? '3px' : `${(valor / ESCALA_MAX) * 100}%` }} />;
}

// ---------- Mapa de calor (sequencial, uma só matiz: esmeralda clara → escura) ----------

const RAMPA = ['#ecfdf5', '#d1fae5', '#a7f3d0', '#6ee7b7', '#34d399', '#10b981', '#059669', '#047857'];

/** Cor de fundo e de texto de uma célula para uma nota de 0 a 5 (null = sem nota). */
function corCalor(valor) {
  if (valor == null) return { fundo: 'var(--superficie-2)', texto: 'var(--texto-suave)' };
  const passo = Math.max(0, Math.min(RAMPA.length - 1, Math.round((valor / ESCALA_MAX) * (RAMPA.length - 1))));
  // Texto escuro tem contraste suficiente até o penúltimo passo; no mais escuro, texto branco
  return { fundo: RAMPA[passo], texto: passo === RAMPA.length - 1 ? '#ffffff' : 'var(--texto)' };
}

export function CelulaCalor({ valor, titulo, forte }) {
  const { fundo, texto } = corCalor(valor);
  return (
    <td className="num celula-calor" style={{ background: fundo, color: texto }} title={titulo}>
      {forte ? <strong>{valor == null ? '—' : fmtNum(valor)}</strong> : (valor ?? '—')}
    </td>
  );
}

export function LegendaCalor() {
  return (
    <div className="legenda-calor" aria-hidden="true">
      <span>0</span>
      {RAMPA.map((c) => <i key={c} style={{ background: c }} />)}
      <span>5</span>
      <span className="legenda-calor-vazio"><i /> sem nota</span>
    </div>
  );
}
