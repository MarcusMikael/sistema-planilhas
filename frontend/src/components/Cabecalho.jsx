import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  Bell, CalendarClock, ChevronDown, ChevronRight, ClipboardCheck, LogOut, MessageSquareHeart, TrendingUp, UserCog,
} from 'lucide-react';
import { useAuth } from '../auth.jsx';
import { useApi } from '../hooks.js';
import { ROTULOS, fmtData, hojeIso } from '../format.js';

const ICONE = { strokeWidth: 1.5, 'aria-hidden': true };

/**
 * Cabeçalho: marca, trilha da página atual, ciclo aberto com o prazo, sino de pendências e menu do usuário.
 * `grupos` é o mesmo menu da barra lateral; `inicio` recebe o botão da gaveta (telas pequenas).
 */
export default function Cabecalho({ grupos, inicio }) {
  return (
    <header className="topo-movel">
      {inicio}
      <Link to="/" className="marca" aria-label="SDPP, ir para o início">
        <span className="marca-logo" aria-hidden="true"><TrendingUp size={17} {...ICONE} /></span>
        <strong>SDPP</strong>
      </Link>
      <Trilha grupos={grupos} />
      <span className="espaco" />
      <CicloAtual />
      <Pendencias />
      <MenuUsuario />
    </header>
  );
}

/** "Desempenho › Matriz Nine Box": o item do menu que corresponde à rota (o prefixo mais longo). */
function Trilha({ grupos }) {
  const { pathname } = useLocation();
  let atual = null;
  for (const g of grupos) {
    for (const item of g.itens) {
      const casa = pathname === item.para || (item.para !== '/' && pathname.startsWith(`${item.para}/`));
      if (casa && (!atual || item.para.length > atual.item.para.length)) atual = { grupo: g.titulo, item };
    }
  }
  if (!atual) return null;
  const detalhe = pathname !== atual.item.para;
  return (
    <nav className="trilha" aria-label="Você está em">
      <span className="trilha-divisor" aria-hidden="true" />
      {atual.grupo && <><span>{atual.grupo}</span><ChevronRight size={13} {...ICONE} /></>}
      {detalhe ? <Link to={atual.item.para}>{atual.item.rotulo}</Link>
        : <span className="trilha-atual" aria-current="page">{atual.item.rotulo}</span>}
    </nav>
  );
}

/** Ciclo liberado para avaliações e quantos dias faltam para o prazo. */
function CicloAtual() {
  const ciclos = useApi('/ciclos');
  // Data local (toISOString seria UTC: depois das 21h em Brasília já daria o dia seguinte)
  const hoje = hojeIso();
  const ciclo = (ciclos.data || []).find((c) => c.status === 'LIBERADO' && c.dataInicio <= hoje && c.dataFim >= hoje);
  if (!ciclo) return null;
  const dias = Math.round((new Date(`${ciclo.dataFim}T00:00:00`) - new Date(`${hoje}T00:00:00`)) / 86400000);
  const prazo = dias === 0 ? 'termina hoje' : dias === 1 ? '1 dia' : `${dias} dias`;
  return (
    <span className="chip-ciclo" title={`${ciclo.nome}: avaliações até ${fmtData(ciclo.dataFim)}`}>
      <CalendarClock size={14} {...ICONE} />
      <span className="chip-ciclo-texto">{ciclo.nome} · {prazo}</span>
    </span>
  );
}

/** Fecha um menu suspenso ao clicar fora dele ou apertar Esc (devolvendo o foco ao botão). */
function useSuspenso() {
  const [aberto, setAberto] = useState(false);
  const raiz = useRef(null);
  const botao = useRef(null);
  useEffect(() => {
    if (!aberto) return undefined;
    const fora = (e) => !raiz.current?.contains(e.target) && setAberto(false);
    const tecla = (e) => {
      if (e.key === 'Escape') {
        setAberto(false);
        botao.current?.focus();
      }
    };
    document.addEventListener('mousedown', fora);
    document.addEventListener('keydown', tecla);
    return () => {
      document.removeEventListener('mousedown', fora);
      document.removeEventListener('keydown', tecla);
    };
  }, [aberto]);
  return { aberto, setAberto, raiz, botao };
}

/** Sino com as avaliações a fazer e as pesquisas de clima a responder (as mesmas do Painel). */
function Pendencias() {
  const { usuario } = useAuth();
  const { pathname } = useLocation();
  const { aberto, setAberto, raiz, botao } = useSuspenso();
  const pend = useApi(usuario.colaboradorId ? '/avaliacoes/pendentes' : null);
  const clima = useApi(usuario.colaboradorId ? '/pesquisas-clima' : null);

  // Atualiza ao trocar de página (ex.: depois de concluir uma avaliação) e fecha a lista
  const [rota, setRota] = useState(pathname);
  if (rota !== pathname) {
    setRota(pathname);
    setAberto(false);
    pend.recarregar();
    clima.recarregar();
  }

  if (!usuario.colaboradorId) return null;
  const avaliacoes = (pend.data || []).filter((p) => p.status !== 'CONCLUIDA');
  const pesquisas = (clima.data || []).filter((p) => p.abertaParaResposta && p.jaRespondeu === false);
  const total = avaliacoes.length + pesquisas.length;
  const rotulo = total === 0 ? 'Pendências: nenhuma' : `Pendências: ${total}`;

  return (
    <div className="suspenso" ref={raiz}>
      <button ref={botao} type="button" className="btn-cabecalho sino" aria-label={rotulo} aria-expanded={aberto}
        aria-haspopup="true" onClick={() => setAberto((a) => !a)}>
        <Bell size={19} {...ICONE} />
        {total > 0 && <span className="sino-contador" aria-hidden="true">{total > 9 ? '9+' : total}</span>}
      </button>
      {aberto && (
        <div className="suspenso-painel painel-pendencias">
          <div className="suspenso-titulo">Suas pendências</div>
          {total === 0 ? <p className="texto-2 pequeno suspenso-vazio">Nada pendente no momento.</p> : (
            <ul>
              {avaliacoes.map((p) => (
                <li key={`${p.cicloId}-${p.colaboradorId}-${p.tipo}`}>
                  <Link to="/avaliacoes">
                    <ClipboardCheck size={16} {...ICONE} />
                    <span>
                      {ROTULOS.tipoAvaliacao[p.tipo]}{p.tipo === 'GESTOR' && ` · ${p.colaboradorNome}`}
                      <small>{p.cicloNome} · até {fmtData(p.cicloDataFim)}</small>
                    </span>
                  </Link>
                </li>
              ))}
              {pesquisas.map((p) => (
                <li key={`clima-${p.id}`}>
                  <Link to={`/clima/${p.id}/responder`}>
                    <MessageSquareHeart size={16} {...ICONE} />
                    <span>Pesquisa de clima<small>{p.titulo} · até {fmtData(p.dataFim)}</small></span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

/** Avatar com as iniciais (anel na cor do perfil), nome e perfil; abre Minha conta e Sair. */
function MenuUsuario() {
  const { usuario, sair } = useAuth();
  const { pathname } = useLocation();
  const { aberto, setAberto, raiz, botao } = useSuspenso();
  const [rota, setRota] = useState(pathname);
  if (rota !== pathname) {
    setRota(pathname);
    setAberto(false);
  }
  const iniciais = usuario.nome.split(/\s+/).filter(Boolean).map((p) => p[0]).filter((_, i, a) => i === 0 || i === a.length - 1)
    .join('').toUpperCase();
  const perfil = ROTULOS.perfil[usuario.perfil];

  return (
    <div className="suspenso" ref={raiz}>
      <button ref={botao} type="button" className="btn-cabecalho usuario" aria-expanded={aberto} aria-haspopup="true"
        aria-label={`${usuario.nome}, perfil ${perfil}. Abrir menu da conta`} onClick={() => setAberto((a) => !a)}>
        <span className={`avatar perfil-${usuario.perfil}`} aria-hidden="true">{iniciais}</span>
        <span className="usuario-texto" aria-hidden="true">
          <span className="usuario-nome">{usuario.nome}</span>
          <span className={`usuario-perfil perfil-${usuario.perfil}`}>{perfil}</span>
        </span>
        <ChevronDown size={15} {...ICONE} />
      </button>
      {aberto && (
        <div className="suspenso-painel painel-usuario">
          <div className="suspenso-titulo">{usuario.email}</div>
          <ul>
            <li><Link to="/conta"><UserCog size={16} {...ICONE} /><span>Minha conta</span></Link></li>
            <li><button type="button" onClick={sair}><LogOut size={16} {...ICONE} /><span>Sair</span></button></li>
          </ul>
        </div>
      )}
    </div>
  );
}
