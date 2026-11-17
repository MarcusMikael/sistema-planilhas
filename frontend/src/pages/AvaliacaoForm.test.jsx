import { act, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth.jsx';
import { mockFetch, resposta } from '../test/fetch.js';
import AvaliacaoForm from './AvaliacaoForm.jsx';

const item = (id, nome, tipo, nota = null) => ({
  cicloCompetenciaId: id, competenciaId: id, nome, descricao: `Descrição de ${nome}`, tipo, nota, comentario: null,
});

const avaliacao = {
  id: 8, cicloId: 2, cicloNome: 'Ciclo 2026/1', cicloStatus: 'LIBERADO', colaboradorId: 5, colaboradorNome: 'Beatriz Alves',
  colaboradorCargo: 'Analista de Dados', avaliadorId: 2, avaliadorNome: 'Carlos Lima', avaliadorCargo: 'Gerente',
  tipo: 'GESTOR', status: 'EM_ANDAMENTO', dataAvaliacao: null, preenchidas: 0, total: 2,
  itens: [item(1, 'Comunicação', 'COMPORTAMENTAL'), item(2, 'Planilhas', 'TECNICA')],
};

function montar() {
  const fetch = mockFetch({
    'GET /auth/me': resposta(200, { nome: 'Carlos Lima', perfil: 'GESTOR', colaboradorId: 2 }),
    'GET /avaliacoes/8': resposta(200, avaliacao),
    'PUT /avaliacoes/8/notas': (opcoes) => {
      const { notas } = JSON.parse(opcoes.body);
      return resposta(200, { ...avaliacao, itens: avaliacao.itens.map((i, k) => ({ ...i, nota: notas[k].nota })) });
    },
  });
  const router = createMemoryRouter([{ path: '/avaliacoes/:id', element: <AvaliacaoForm /> }],
    { initialEntries: ['/avaliacoes/8'] });
  render(<AuthProvider><RouterProvider router={router} /></AuthProvider>);
  return fetch;
}

describe('AvaliacaoForm: rascunho automático', () => {
  afterEach(() => vi.useRealTimers());

  it('salva sozinho 3 s depois da última alteração, sem perder o que foi editado durante o envio', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    let responderPut;
    const fetch = mockFetch({
      'GET /auth/me': resposta(200, { nome: 'Carlos Lima', perfil: 'GESTOR', colaboradorId: 2 }),
      'GET /avaliacoes/8': resposta(200, avaliacao),
      'PUT /avaliacoes/8/notas': () => new Promise((ok) => { responderPut = ok; }),
    });
    const router = createMemoryRouter([{ path: '/avaliacoes/:id', element: <AvaliacaoForm /> }],
      { initialEntries: ['/avaliacoes/8'] });
    render(<AuthProvider><RouterProvider router={router} /></AuthProvider>);

    const comunicacao = await screen.findByRole('radiogroup', { name: 'Nota para Comunicação' });
    await usuario.click(within(comunicacao).getByRole('button', { name: '4' }));
    expect(screen.getByText('Alterações ainda não salvas')).toBeInTheDocument();
    const puts = () => fetch.mock.calls.filter(([, o]) => o.method === 'PUT').length;

    await act(async () => vi.advanceTimersByTime(2900));
    expect(puts()).toBe(0);
    await act(async () => vi.advanceTimersByTime(200));
    expect(puts()).toBe(1);

    // Enquanto o salvamento está em andamento, a pessoa marca outra nota
    const planilhas = screen.getByRole('radiogroup', { name: 'Nota para Planilhas' });
    await usuario.click(within(planilhas).getByRole('button', { name: '2' }));
    await act(async () => responderPut(resposta(200, {
      ...avaliacao, itens: [{ ...avaliacao.itens[0], nota: 4 }, avaliacao.itens[1]],
    })));

    // A nota marcada durante o envio continua na tela e continua pendente de salvar
    expect(within(planilhas).getByRole('button', { name: '2' })).toHaveAttribute('aria-pressed', 'true');
    expect(within(comunicacao).getByRole('button', { name: '4' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByText('Alterações ainda não salvas')).toBeInTheDocument();
  });
});

describe('AvaliacaoForm', () => {
  it('seleciona a nota no controle segmentado e libera a conclusão só com todas as notas', async () => {
    montar();
    const comunicacao = await screen.findByRole('radiogroup', { name: 'Nota para Comunicação' });
    const planilhas = screen.getByRole('radiogroup', { name: 'Nota para Planilhas' });
    const concluir = screen.getByRole('button', { name: 'Concluir avaliação' });
    expect(concluir).toBeDisabled();

    await userEvent.click(within(comunicacao).getByRole('button', { name: '4' }));
    expect(within(comunicacao).getByRole('button', { name: '4' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByText('1 de 2 notas preenchidas')).toBeInTheDocument();
    expect(concluir).toBeDisabled();

    await userEvent.click(within(planilhas).getByRole('button', { name: '0' }));
    expect(screen.getByText('2 de 2 notas preenchidas')).toBeInTheDocument();
    expect(concluir).toBeEnabled();
  });

  it('clicar de novo na nota selecionada a desmarca', async () => {
    montar();
    const grupo = await screen.findByRole('radiogroup', { name: 'Nota para Comunicação' });
    const tres = within(grupo).getByRole('button', { name: '3' });
    await userEvent.click(tres);
    await userEvent.click(tres);
    expect(tres).toHaveAttribute('aria-pressed', 'false');
  });

  it('salva o rascunho com as notas escolhidas', async () => {
    const fetch = montar();
    const grupo = await screen.findByRole('radiogroup', { name: 'Nota para Comunicação' });
    await userEvent.click(within(grupo).getByRole('button', { name: '5' }));
    await userEvent.click(screen.getByRole('button', { name: 'Salvar rascunho' }));
    expect(await screen.findByText('Rascunho salvo.')).toBeInTheDocument();
    const [, opcoes] = fetch.mock.calls.find(([url, o]) => url.endsWith('/avaliacoes/8/notas') && o.method === 'PUT');
    expect(JSON.parse(opcoes.body).notas[0]).toEqual({ cicloCompetenciaId: 1, nota: 5, comentario: null });
  });
});
