import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from '../auth.jsx';
import { mockFetch, resposta } from '../test/fetch.js';
import Cabecalho from './Cabecalho.jsx';

// Data local (mesma regra do componente), deslocada em dias
const iso = (dias) => {
  const d = new Date();
  d.setDate(d.getDate() + dias);
  const p = (x) => String(x).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
};

const grupos = [
  { titulo: null, itens: [{ para: '/', rotulo: 'Painel' }] },
  { titulo: 'Desempenho', itens: [{ para: '/nine-box', rotulo: 'Matriz Nine Box' }] },
  { titulo: 'Desenvolvimento', itens: [{ para: '/pdis', rotulo: 'PDIs' }] },
];

function montar(caminho, { pendentes = [], pesquisas = [], ciclos } = {}) {
  const fetch = mockFetch({
    'GET /auth/me': resposta(200, { nome: 'Carlos Lima', email: 'gestor@sdpp.local', perfil: 'GESTOR', colaboradorId: 2 }),
    'GET /ciclos': resposta(200, ciclos ?? [
      { id: 1, nome: 'Ciclo 2025/2', status: 'RESULTADOS_LIBERADOS', dataInicio: iso(-200), dataFim: iso(-150) },
      { id: 2, nome: 'Ciclo 2026/1', status: 'LIBERADO', dataInicio: iso(-10), dataFim: iso(45) },
    ]),
    'GET /avaliacoes/pendentes': resposta(200, pendentes),
    'GET /pesquisas-clima': resposta(200, pesquisas),
    'POST /auth/logout': resposta(204),
  });
  // Como no app, o cabeçalho só aparece com o usuário já carregado
  function ComUsuario() {
    const { usuario } = useAuth();
    return usuario ? <Cabecalho grupos={grupos} /> : null;
  }
  const router = createMemoryRouter([{ path: '*', element: <ComUsuario /> }], { initialEntries: [caminho] });
  render(<AuthProvider><RouterProvider router={router} /></AuthProvider>);
  return fetch;
}

describe('Cabecalho', () => {
  it('mostra a trilha da página atual e o ciclo aberto com o prazo', async () => {
    montar('/nine-box');
    const trilha = await screen.findByRole('navigation', { name: 'Você está em' });
    expect(trilha).toHaveTextContent('Desempenho');
    expect(within(trilha).getByText('Matriz Nine Box')).toHaveAttribute('aria-current', 'page');
    expect(await screen.findByText('Ciclo 2026/1 · 45 dias')).toBeInTheDocument();
  });

  it('usa a data local: às 23h30 do último dia, o prazo ainda é "termina hoje"', async () => {
    // 23h30 em Brasília = 02h30 do dia seguinte em UTC
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(new Date(2026, 10, 17, 23, 30));
    try {
      montar('/', { ciclos: [{ id: 2, nome: 'Ciclo 2026/1', status: 'LIBERADO', dataInicio: '2026-11-01', dataFim: '2026-11-17' }] });
      expect(await screen.findByText('Ciclo 2026/1 · termina hoje')).toBeInTheDocument();
    } finally {
      vi.useRealTimers();
    }
  });

  it('em uma página de detalhe, a trilha leva de volta à lista', async () => {
    montar('/pdis/7');
    const trilha = await screen.findByRole('navigation', { name: 'Você está em' });
    expect(within(trilha).getByRole('link', { name: 'PDIs' })).toHaveAttribute('href', '/pdis');
  });

  it('sem ciclo em andamento, não mostra o selo do ciclo', async () => {
    montar('/', { ciclos: [] });
    await screen.findByRole('button', { name: /Abrir menu da conta/ });
    expect(screen.queryByText(/Ciclo /)).not.toBeInTheDocument();
  });

  it('o sino conta e lista as pendências', async () => {
    montar('/', {
      pendentes: [
        { cicloId: 2, cicloNome: 'Ciclo 2026/1', cicloDataFim: iso(45), colaboradorId: 2, colaboradorNome: 'Carlos Lima', tipo: 'AUTOAVALIACAO', status: null },
        { cicloId: 2, cicloNome: 'Ciclo 2026/1', cicloDataFim: iso(45), colaboradorId: 5, colaboradorNome: 'Beatriz Alves', tipo: 'GESTOR', status: 'EM_ANDAMENTO' },
        { cicloId: 2, cicloNome: 'Ciclo 2026/1', cicloDataFim: iso(45), colaboradorId: 6, colaboradorNome: 'Pedro Santos', tipo: 'GESTOR', status: 'CONCLUIDA' },
      ],
      pesquisas: [{ id: 3, titulo: 'Clima 2026', dataFim: iso(20), abertaParaResposta: true, jaRespondeu: false }],
    });
    const sino = await screen.findByRole('button', { name: 'Pendências: 3' });
    await userEvent.click(sino);
    expect(screen.getByText('Suas pendências')).toBeInTheDocument();
    expect(screen.getByText(/Beatriz Alves/)).toBeInTheDocument();
    expect(screen.queryByText(/Pedro Santos/)).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Pesquisa de clima/ })).toHaveAttribute('href', '/clima/3/responder');
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByText('Suas pendências')).not.toBeInTheDocument();
    expect(sino).toHaveFocus();
  });

  it('menu do usuário com iniciais, Minha conta e Sair', async () => {
    const fetch = montar('/');
    const botao = await screen.findByRole('button', { name: /Carlos Lima, perfil Gestor/ });
    expect(within(botao).getByText('CL')).toHaveClass('avatar', 'perfil-GESTOR');
    await userEvent.click(botao);
    expect(screen.getByRole('link', { name: 'Minha conta' })).toHaveAttribute('href', '/conta');
    await userEvent.click(screen.getByRole('button', { name: 'Sair' }));
    expect(fetch.mock.calls.some(([url, o]) => url.endsWith('/auth/logout') && o.method === 'POST')).toBe(true);
  });
});
