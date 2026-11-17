import { act, render, renderHook, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, Link, Outlet, RouterProvider } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { useApi, useAvisoSaida, useBloqueioSaida } from './hooks.js';
import { mockFetch, resposta } from './test/fetch.js';

describe('useApi', () => {
  it('ignora a resposta atrasada de um path antigo', async () => {
    let liberarCiclo1;
    mockFetch({
      'GET /ciclos/1': () => new Promise((ok) => { liberarCiclo1 = () => ok(resposta(200, { nome: 'Ciclo 1' })); }),
      'GET /ciclos/2': resposta(200, { nome: 'Ciclo 2' }),
    });
    const { result, rerender } = renderHook(({ path }) => useApi(path), { initialProps: { path: '/ciclos/1' } });
    expect(result.current.carregando).toBe(true);

    rerender({ path: '/ciclos/2' });
    await waitFor(() => expect(result.current.data).toEqual({ nome: 'Ciclo 2' }));

    // A resposta do ciclo 1 chega depois e não pode sobrescrever o ciclo 2
    await act(async () => liberarCiclo1());
    expect(result.current.data).toEqual({ nome: 'Ciclo 2' });
    expect(result.current.carregando).toBe(false);
  });

  it('expõe o erro da API e recarrega sob demanda', async () => {
    let chamadas = 0;
    mockFetch({
      'GET /x': () => (++chamadas === 1 ? resposta(500, { mensagem: 'Falhou' }) : resposta(200, { ok: true })),
    });
    const { result } = renderHook(() => useApi('/x'));
    await waitFor(() => expect(result.current.erro?.message).toBe('Falhou'));
    act(() => result.current.recarregar());
    await waitFor(() => expect(result.current.data).toEqual({ ok: true }));
    expect(result.current.erro).toBeNull();
  });

  it('não faz requisição com path nulo', () => {
    const fetch = mockFetch({});
    const { result } = renderHook(() => useApi(null));
    expect(result.current.carregando).toBe(false);
    expect(fetch).not.toHaveBeenCalled();
  });
});

describe('aviso de alterações não salvas', () => {
  function Tela({ sujo }) {
    useAvisoSaida(sujo);
    return <Link to="/outra">Ir</Link>;
  }
  function Raiz() {
    useBloqueioSaida();
    return <Outlet />;
  }
  const montar = (sujo) => {
    const router = createMemoryRouter([
      { element: <Raiz />, children: [{ path: '/', element: <Tela sujo={sujo} /> }, { path: '/outra', element: <p>Outra tela</p> }] },
    ]);
    render(<RouterProvider router={router} />);
  };

  it('navega sem perguntar quando não há alterações', async () => {
    const confirmar = vi.spyOn(window, 'confirm');
    montar(false);
    await userEvent.click(screen.getByText('Ir'));
    expect(await screen.findByText('Outra tela')).toBeInTheDocument();
    expect(confirmar).not.toHaveBeenCalled();
  });

  it('bloqueia a navegação se o usuário cancelar', async () => {
    const confirmar = vi.spyOn(window, 'confirm').mockReturnValue(false);
    montar(true);
    await userEvent.click(screen.getByText('Ir'));
    await waitFor(() => expect(confirmar).toHaveBeenCalled());
    expect(screen.queryByText('Outra tela')).not.toBeInTheDocument();
  });

  it('segue a navegação se o usuário confirmar', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    montar(true);
    await userEvent.click(screen.getByText('Ir'));
    expect(await screen.findByText('Outra tela')).toBeInTheDocument();
  });
});
