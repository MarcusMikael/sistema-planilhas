import { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuth } from '../auth.jsx';
import { ROTULOS } from '../format.js';

/** Menu organizado pelos três perfis (RNF-USA-01). */
function itensMenu({ isRH, isGestor, usuario }) {
  const temColaborador = Boolean(usuario.colaboradorId);
  const grupos = [];
  grupos.push({
    titulo: null,
    itens: [{ para: '/', rotulo: isRH || isGestor ? 'Painel' : 'Início', fim: true }],
  });
  if (isRH) {
    grupos.push({
      titulo: 'Cadastros',
      itens: [
        { para: '/colaboradores', rotulo: 'Colaboradores' },
        { para: '/usuarios', rotulo: 'Usuários e acessos' },
        { para: '/cadastros', rotulo: 'Departamentos e cargos' },
      ],
    });
  }
  const desempenho = [];
  if (isRH) {
    desempenho.push({ para: '/competencias', rotulo: 'Competências' });
    desempenho.push({ para: '/ciclos', rotulo: 'Ciclos de avaliação' });
  }
  if (temColaborador) {
    desempenho.push({ para: '/avaliacoes', rotulo: isGestor || usuario.ehGestorDeEquipe ? 'Avaliações' : 'Minha autoavaliação' });
  }
  desempenho.push({ para: '/resultados', rotulo: isRH || isGestor ? 'Resultados' : 'Meus resultados' });
  if (isRH || isGestor) {
    desempenho.push({ para: '/mapa-competencias', rotulo: 'Mapa de competências' });
    desempenho.push({ para: '/nine-box', rotulo: 'Matriz Nine Box' });
  }
  grupos.push({ titulo: 'Desempenho', itens: desempenho });

  const desenvolvimento = [{ para: '/pdis', rotulo: isRH || isGestor ? 'PDIs' : 'Meu PDI' }];
  if (isGestor) desenvolvimento.push({ para: '/colaboradores', rotulo: 'Minha equipe' });
  desenvolvimento.push({ para: '/historico', rotulo: 'Histórico' });
  grupos.push({ titulo: 'Desenvolvimento', itens: desenvolvimento });

  grupos.push({ titulo: 'Clima', itens: [{ para: '/clima', rotulo: 'Pesquisa de clima' }] });
  grupos.push({ titulo: 'Conta', itens: [{ para: '/conta', rotulo: 'Minha conta' }] });
  return grupos;
}

export default function Layout() {
  const auth = useAuth();
  const [aberto, setAberto] = useState(false);
  const local = useLocation();
  useEffect(() => setAberto(false), [local.pathname]);

  return (
    <div className="app">
      <div className="topo-movel">
        <button onClick={() => setAberto((a) => !a)} aria-label="Abrir menu">☰</button>
        <strong>SDPP</strong>
      </div>
      <nav className={`lateral ${aberto ? 'aberta' : ''}`} aria-label="Menu principal">
        <div className="marca">
          <div className="marca-logo">S</div>
          <div>
            <strong>SDPP</strong>
            <small>Desenvolvimento e Performance</small>
          </div>
        </div>
        {itensMenu(auth).map((g, i) => (
          <div key={i}>
            {g.titulo && <div className="menu-grupo">{g.titulo}</div>}
            {g.itens.map((it) => (
              <NavLink key={it.para + it.rotulo} to={it.para} end={it.fim}
                className={({ isActive }) => (isActive ? 'ativo' : undefined)}>
                {it.rotulo}
              </NavLink>
            ))}
          </div>
        ))}
        <div className="lateral-rodape">
          <strong>{auth.usuario.nome}</strong>
          <span>{ROTULOS.perfil[auth.usuario.perfil]}</span>
          <div>
            <button onClick={auth.sair}>Sair</button>
          </div>
        </div>
      </nav>
      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  );
}
