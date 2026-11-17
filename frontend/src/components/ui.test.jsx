import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { api, LIMITE_LENTO_MS } from '../api.js';
import { mockFetch, resposta } from '../test/fetch.js';
import { useState } from 'react';
import { AvisoServidorLento, Badge, Carregando, Modal, Pagina, Progresso } from './ui.jsx';

describe('Badge', () => {
  it('usa o rótulo do grupo e a cor do status', () => {
    render(<Badge status="EM_ANDAMENTO" grupo="statusAcao" />);
    const badge = screen.getByText('Em andamento');
    expect(badge).toHaveClass('badge', 'badge-aviso');
  });

  it('aceita texto próprio', () => {
    render(<Badge status="ERRO">Atrasada</Badge>);
    expect(screen.getByText('Atrasada')).toHaveClass('badge-erro');
  });
});

describe('Progresso', () => {
  it('limita o valor entre 0 e 100', () => {
    const { container, rerender } = render(<Progresso valor={140} />);
    expect(screen.getByText('100%')).toBeInTheDocument();
    expect(container.querySelector('.progresso-barra')).toHaveStyle({ width: '100%' });
    rerender(<Progresso valor={-5} />);
    expect(screen.getByText('0%')).toBeInTheDocument();
  });
});

describe('AvisoServidorLento', () => {
  it('aparece só quando uma requisição passa de 3 segundos e some quando ela termina', async () => {
    vi.useFakeTimers();
    try {
      let responder;
      mockFetch({ 'GET /auth/me': () => new Promise((ok) => { responder = ok; }) });
      render(<AvisoServidorLento />);
      const pendente = api.get('/auth/me');

      await act(async () => vi.advanceTimersByTime(LIMITE_LENTO_MS - 100));
      expect(screen.queryByText('Acordando o servidor…')).not.toBeInTheDocument();

      await act(async () => vi.advanceTimersByTime(200));
      expect(screen.getByText('Acordando o servidor…')).toBeInTheDocument();

      await act(async () => {
        responder(resposta(200, { nome: 'Ana' }));
        await pendente;
      });
      expect(screen.queryByText('Acordando o servidor…')).not.toBeInTheDocument();
    } finally {
      vi.useRealTimers();
    }
  });
});

describe('Pagina e Carregando', () => {
  it('define o título da aba do navegador', () => {
    render(<Pagina titulo="Matriz Nine Box"><p>conteúdo</p></Pagina>);
    expect(document.title).toBe('Matriz Nine Box · SDPP');
  });

  it('mostra o esqueleto com texto para leitores de tela', () => {
    const { container } = render(<Carregando />);
    expect(screen.getByRole('status')).toHaveTextContent('Carregando…');
    expect(container.querySelectorAll('.esqueleto-linha').length).toBeGreaterThan(0);
  });
});

describe('Modal: foco', () => {
  function Tela() {
    const [aberto, setAberto] = useState(false);
    return (
      <>
        <button onClick={() => setAberto(true)}>Abrir</button>
        <Modal titulo="Editar" aberto={aberto} onFechar={() => setAberto(false)}>
          <input aria-label="Nome" />
        </Modal>
      </>
    );
  }

  it('leva o foco para o primeiro campo e devolve ao botão que abriu', async () => {
    render(<Tela />);
    const abrir = screen.getByRole('button', { name: 'Abrir' });
    await userEvent.click(abrir);
    expect(screen.getByLabelText('Nome')).toHaveFocus();
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(abrir).toHaveFocus();
  });
});

describe('Modal', () => {
  it('fecha direto quando nada foi editado', async () => {
    const onFechar = vi.fn();
    const confirmar = vi.spyOn(window, 'confirm');
    render(<Modal titulo="Teste" aberto onFechar={onFechar}><input aria-label="Nome" /></Modal>);
    await userEvent.click(screen.getByRole('button', { name: 'Fechar' }));
    expect(onFechar).toHaveBeenCalled();
    expect(confirmar).not.toHaveBeenCalled();
  });

  it('pede confirmação antes de descartar edições', async () => {
    const onFechar = vi.fn();
    const confirmar = vi.spyOn(window, 'confirm').mockReturnValue(false);
    render(<Modal titulo="Teste" aberto onFechar={onFechar}><input aria-label="Nome" /></Modal>);
    await userEvent.type(screen.getByLabelText('Nome'), 'Ana');
    await userEvent.keyboard('{Escape}');
    expect(confirmar).toHaveBeenCalled();
    expect(onFechar).not.toHaveBeenCalled();

    confirmar.mockReturnValue(true);
    await userEvent.click(screen.getByRole('button', { name: 'Fechar' }));
    expect(onFechar).toHaveBeenCalled();
  });
});
