import { Suspense, useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  Award, Building2, CalendarRange, ChartColumn, ClipboardCheck, History, KeyRound, LayoutDashboard, LayoutGrid,
  Menu, MessageSquareHeart, Sprout, Table2, UserCog, Users, X,
} from 'lucide-react';
import { useAuth } from '../auth.jsx';
import { useBloqueioSaida } from '../hooks.js';
import Cabecalho from './Cabecalho.jsx';
import ErroBoundary from './ErroBoundary.jsx';
import { Carregando } from './ui.jsx';

const ICONE = { size: 18, strokeWidth: 1.5, 'aria-hidden': true };

/** Menu organizado pelos três perfis (RNF-USA-01). */
function itensMenu({ isRH, isGestor, usuario }) {
  const temColaborador = Boolean(usuario.colaboradorId);
  const grupos = [];
  grupos.push({
    titulo: null,
    itens: [{ para: '/', rotulo: isRH || isGestor ? 'Painel' : 'Início', fim: true, Icone: LayoutDashboard }],
  });
  if (isRH) {
    grupos.push({
      titulo: 'Cadastros',
      itens: [
        { para: '/colaboradores', rotulo: 'Colaboradores', Icone: Users },
        { para: '/usuarios', rotulo: 'Usuários e acessos', Icone: KeyRound },
        { para: '/cadastros', rotulo: 'Departamentos e cargos', Icone: Building2 },
      ],
    });
  }
  const desempenho = [];
  if (isRH) {
    desempenho.push({ para: '/competencias', rotulo: 'Competências', Icone: Award });
    desempenho.push({ para: '/ciclos', rotulo: 'Ciclos de avaliação', Icone: CalendarRange });
  }
  if (temColaborador) {
    desempenho.push({
      para: '/avaliacoes', rotulo: isGestor || usuario.ehGestorDeEquipe ? 'Avaliações' : 'Minha autoavaliação',
      Icone: ClipboardCheck,
    });
  }
  desempenho.push({ para: '/resultados', rotulo: isRH || isGestor ? 'Resultados' : 'Meus resultados', Icone: ChartColumn });
  if (isRH || isGestor) {
    desempenho.push({ para: '/mapa-competencias', rotulo: 'Mapa de competências', Icone: Table2 });
    desempenho.push({ para: '/nine-box', rotulo: 'Matriz Nine Box', Icone: LayoutGrid });
  }
  grupos.push({ titulo: 'Desempenho', itens: desempenho });

  const desenvolvimento = [{ para: '/pdis', rotulo: isRH || isGestor ? 'PDIs' : 'Meu PDI', Icone: Sprout }];
  if (isGestor) desenvolvimento.push({ para: '/colaboradores', rotulo: 'Minha equipe', Icone: Users });
  desenvolvimento.push({ para: '/historico', rotulo: 'Histórico', Icone: History });
  grupos.push({ titulo: 'Desenvolvimento', itens: desenvolvimento });

  grupos.push({ titulo: 'Clima', itens: [{ para: '/clima', rotulo: 'Pesquisa de clima', Icone: MessageSquareHeart }] });
  grupos.push({ titulo: 'Conta', itens: [{ para: '/conta', rotulo: 'Minha conta', Icone: UserCog }] });
  return grupos;
}

export default function Layout() {
  const auth = useAuth();
  const [aberto, setAberto] = useState(false);
  const local = useLocation();
  const botaoMenu = useRef(null);
  const menu = useRef(null);
  const grupos = itensMenu(auth);
  useBloqueioSaida();

  // Fecha a gaveta ao trocar de página (ajuste de estado durante a renderização, sem efeito)
  const [rota, setRota] = useState(local.pathname);
  if (rota !== local.pathname) {
    setRota(local.pathname);
    setAberto(false);
  }

  // Gaveta (telas pequenas): Esc fecha e o foco volta ao botão; ao abrir, o foco vai para o primeiro link.
  useEffect(() => {
    if (!aberto) return undefined;
    menu.current?.querySelector('a')?.focus();
    const tecla = (e) => {
      if (e.key === 'Escape') {
        setAberto(false);
        botaoMenu.current?.focus();
      }
    };
    window.addEventListener('keydown', tecla);
    return () => window.removeEventListener('keydown', tecla);
  }, [aberto]);

  return (
    <div className="app">
      <a className="pular" href="#conteudo">Pular para o conteúdo</a>
      <Cabecalho grupos={grupos} inicio={(
        <button ref={botaoMenu} className="btn-menu" onClick={() => setAberto((a) => !a)}
          aria-label={aberto ? 'Fechar menu' : 'Abrir menu'} aria-expanded={aberto} aria-controls="menu-principal">
          {aberto ? <X {...ICONE} /> : <Menu {...ICONE} />}
        </button>
      )} />
      {aberto && <div className="lateral-fundo" onClick={() => setAberto(false)} aria-hidden="true" />}
      <nav id="menu-principal" ref={menu} className={`lateral ${aberto ? 'aberta' : ''}`} aria-label="Menu principal">
        {grupos.map((g, i) => (
          <div key={i}>
            {g.titulo && <div className="menu-grupo">{g.titulo}</div>}
            {g.itens.map(({ para, rotulo, fim, Icone }) => (
              <NavLink key={para + rotulo} to={para} end={fim}
                className={({ isActive }) => (isActive ? 'ativo' : undefined)}>
                <Icone {...ICONE} />
                {rotulo}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
      <main className="conteudo" id="conteudo" tabIndex={-1}>
        <ErroBoundary key={local.pathname}>
          <Suspense fallback={<Carregando />}>
            <Outlet />
          </Suspense>
        </ErroBoundary>
      </main>
    </div>
  );
}
