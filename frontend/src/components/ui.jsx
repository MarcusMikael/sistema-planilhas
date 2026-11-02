import { useEffect } from 'react';
import { ROTULOS, tomStatus } from '../format.js';

export function Carregando({ texto = 'Carregando…' }) {
  return <div className="carregando" role="status">{texto}</div>;
}

export function Alerta({ erro, tipo = 'erro', children }) {
  if (!erro && !children) return null;
  const msg = erro ? erro.message : children;
  return (
    <div className={`alerta alerta-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {msg}
      {erro?.campos && (
        <ul>
          {Object.entries(erro.campos).map(([campo, m]) => (
            <li key={campo}>{m}</li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function Vazio({ children }) {
  return <div className="vazio">{children}</div>;
}

export function Badge({ status, grupo, children }) {
  const texto = children ?? (grupo ? ROTULOS[grupo]?.[status] ?? status : status);
  return <span className={`badge badge-${tomStatus(status)}`}>{texto}</span>;
}

export function Pagina({ titulo, subtitulo, acoes, children }) {
  return (
    <section className="pagina">
      <header className="pagina-cabecalho">
        <div>
          <h1>{titulo}</h1>
          {subtitulo && <p className="subtitulo">{subtitulo}</p>}
        </div>
        {acoes && <div className="pagina-acoes">{acoes}</div>}
      </header>
      {children}
    </section>
  );
}

export function Cartao({ titulo, acoes, children, className = '' }) {
  return (
    <div className={`cartao ${className}`}>
      {(titulo || acoes) && (
        <div className="cartao-cabecalho">
          {titulo && <h2>{titulo}</h2>}
          {acoes}
        </div>
      )}
      {children}
    </div>
  );
}

export function Modal({ titulo, aberto, onFechar, children, largo }) {
  useEffect(() => {
    if (!aberto) return;
    const esc = (e) => e.key === 'Escape' && onFechar();
    window.addEventListener('keydown', esc);
    return () => window.removeEventListener('keydown', esc);
  }, [aberto, onFechar]);
  if (!aberto) return null;
  return (
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && onFechar()}>
      <div className={`modal ${largo ? 'modal-largo' : ''}`} role="dialog" aria-modal="true" aria-label={titulo}>
        <div className="modal-cabecalho">
          <h2>{titulo}</h2>
          <button className="btn-icone" onClick={onFechar} aria-label="Fechar">×</button>
        </div>
        <div className="modal-corpo">{children}</div>
      </div>
    </div>
  );
}

export function Campo({ rotulo, erro, children, dica }) {
  return (
    <label className="campo">
      <span className="campo-rotulo">{rotulo}</span>
      {children}
      {dica && <span className="campo-dica">{dica}</span>}
      {erro && <span className="campo-erro">{erro}</span>}
    </label>
  );
}

export function Progresso({ valor }) {
  const v = Math.max(0, Math.min(100, valor || 0));
  return (
    <div className="progresso" title={`${v}%`}>
      <div className="progresso-barra" style={{ width: `${v}%` }} />
      <span>{v}%</span>
    </div>
  );
}

export function Indicador({ rotulo, valor, detalhe }) {
  return (
    <div className="indicador">
      <span className="indicador-rotulo">{rotulo}</span>
      <strong className="indicador-valor">{valor}</strong>
      {detalhe && <span className="indicador-detalhe">{detalhe}</span>}
    </div>
  );
}

export function Seletor({ valor, onChange, opcoes, vazio, ...resto }) {
  return (
    <select value={valor ?? ''} onChange={(e) => onChange(e.target.value === '' ? null : e.target.value)} {...resto}>
      {vazio !== undefined && <option value="">{vazio}</option>}
      {opcoes.map((o) => (
        <option key={o.valor} value={o.valor}>
          {o.rotulo}
        </option>
      ))}
    </select>
  );
}
