import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import ErroBoundary from './ErroBoundary.jsx';

function TelaQuebrada() {
  throw new Error('campo nulo inesperado');
}

describe('ErroBoundary', () => {
  it('mostra uma tela de erro em vez de deixar a aplicação em branco', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    render(<ErroBoundary><TelaQuebrada /></ErroBoundary>);
    expect(screen.getByRole('alert')).toHaveTextContent('Algo deu errado nesta tela');
    expect(screen.getByRole('button', { name: /Recarregar a página/ })).toBeInTheDocument();
  });

  it('renderiza normalmente quando não há erro', () => {
    render(<ErroBoundary><p>Conteúdo</p></ErroBoundary>);
    expect(screen.getByText('Conteúdo')).toBeInTheDocument();
  });
});
