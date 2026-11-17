import { useCallback, useEffect, useRef, useState, useSyncExternalStore } from 'react';
import { LoaderCircle, X } from 'lucide-react';
import { observarLentidao, servidorLento } from '../api.js';
import { MSG_ALTERACOES, useAvisoSaida, useTituloAba } from '../hooks.js';
import { ROTULOS, tomStatus } from '../format.js';

const FOCAVEIS = 'a[href], button, input, select, textarea, [tabindex]:not([tabindex="-1"])';

/**
 * Esqueleto de carregamento: blocos no formato aproximado do conteúdo, para a tela não "pular" quando os
 * dados chegam. O texto continua disponível para leitores de tela.
 */
export function Carregando({ texto = 'Carregando…' }) {
  return (
    <div className="esqueleto" role="status" aria-live="polite">
      <span className="so-leitor">{texto}</span>
      <div className="esqueleto-linha esqueleto-titulo" />
      <div className="esqueleto-linha" />
      <div className="esqueleto-linha" />
      <div className="esqueleto-linha esqueleto-curta" />
    </div>
  );
}

/**
 * Aviso exibido quando uma requisição demora (servidor acordando no plano gratuito do Render),
 * para que a tela parada não pareça um erro.
 */
export function AvisoServidorLento() {
  const lento = useSyncExternalStore(observarLentidao, servidorLento);
  return (
    <div className="aviso-lento-regiao" role="status" aria-live="polite">
      {lento && (
        <div className="aviso-lento">
          <LoaderCircle className="girando" size={18} strokeWidth={1.5} aria-hidden="true" />
          <div>
            <strong>Acordando o servidor…</strong>
            <span>Depois de um tempo sem uso, o primeiro acesso pode levar até 1 minuto. Não feche a página.</span>
          </div>
        </div>
      )}
    </div>
  );
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
  useTituloAba(titulo);
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

/**
 * Gaveta lateral. Se o usuário editar algum campo e tentar fechar (Esc, fundo, X) ou sair da página
 * sem enviar o formulário, pede confirmação antes de descartar.
 */
export function Modal({ titulo, aberto, onFechar, children, largo }) {
  const [sujo, setSujo] = useState(false);
  useAvisoSaida(aberto && sujo);

  // Ao fechar, esquece as edições (ajuste de estado durante a renderização, sem efeito)
  if (!aberto && sujo) setSujo(false);

  const fechar = useCallback(() => {
    if (sujo && !window.confirm(MSG_ALTERACOES)) return;
    onFechar();
  }, [sujo, onFechar]);

  useEffect(() => {
    if (!aberto) return;
    const esc = (e) => e.key === 'Escape' && fechar();
    window.addEventListener('keydown', esc);
    return () => window.removeEventListener('keydown', esc);
  }, [aberto, fechar]);

  // Foco: ao abrir, vai para o primeiro campo do modal; Tab e Shift+Tab circulam só dentro dele;
  // ao fechar, volta para o elemento que abriu o modal (ex.: o botão "Editar").
  const dialogo = useRef(null);
  useEffect(() => {
    if (!aberto) return undefined;
    const origem = document.activeElement;
    const focaveis = () => [...(dialogo.current?.querySelectorAll(FOCAVEIS) ?? [])]
      .filter((el) => !el.disabled && el.offsetParent !== null);
    const primeiroCampo = dialogo.current?.querySelector('.modal-corpo :is(input, select, textarea):not([disabled])');
    (primeiroCampo ?? focaveis()[0])?.focus();
    const prender = (e) => {
      if (e.key !== 'Tab') return;
      const lista = focaveis();
      if (lista.length === 0) return;
      const [primeiro, ultimo] = [lista[0], lista[lista.length - 1]];
      if (!dialogo.current.contains(document.activeElement)) {
        e.preventDefault();
        primeiro.focus();
      } else if (e.shiftKey && document.activeElement === primeiro) {
        e.preventDefault();
        ultimo.focus();
      } else if (!e.shiftKey && document.activeElement === ultimo) {
        e.preventDefault();
        primeiro.focus();
      }
    };
    document.addEventListener('keydown', prender);
    return () => {
      document.removeEventListener('keydown', prender);
      if (origem instanceof HTMLElement && document.contains(origem)) origem.focus();
    };
  }, [aberto]);

  if (!aberto) return null;
  return (
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && fechar()}>
      <div ref={dialogo} className={`modal ${largo ? 'modal-largo' : ''}`} role="dialog" aria-modal="true" aria-label={titulo}
        onInput={() => setSujo(true)} onChange={() => setSujo(true)} onSubmit={() => setSujo(false)}>
        <div className="modal-cabecalho">
          <h2>{titulo}</h2>
          <button className="btn-icone" onClick={fechar} aria-label="Fechar"><X size={20} strokeWidth={1.5} aria-hidden="true" /></button>
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
