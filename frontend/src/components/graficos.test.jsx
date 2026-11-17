import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { CelulaCalor, GraficoComparacao } from './graficos.jsx';

const itens = [
  { competencia: 'Comunicação', tipo: 'COMPORTAMENTAL', notaGestor: 4, notaAutoavaliacao: 5 },
  { competencia: 'Planilhas', tipo: 'TECNICA', notaGestor: 0, notaAutoavaliacao: null },
];

describe('GraficoComparacao', () => {
  it('desenha as barras proporcionais à escala de 0 a 5', () => {
    const { container } = render(<GraficoComparacao itens={itens} />);
    const gestor = container.querySelectorAll('.gc-area .gc-barra.gc-gestor');
    expect(gestor[0]).toHaveStyle({ width: '80%' });
    // Nota 0 aparece como um traço mínimo, diferente de "sem nota"
    expect(gestor[1]).toHaveStyle({ width: '3px' });
    expect(container.querySelectorAll('.gc-area .gc-vazia')).toHaveLength(1);
    expect(screen.getByText('Comportamentais')).toBeInTheDocument();
    expect(screen.getByText('Técnicas')).toBeInTheDocument();
  });

  it('mostra os valores no tooltip ao passar o mouse e pelo teclado', async () => {
    render(<GraficoComparacao itens={itens} />);
    const linha = screen.getByLabelText('Comunicação: gestor 4, autoavaliação 5');
    await userEvent.hover(linha);
    expect(screen.getByText('Diferença +1')).toBeInTheDocument();
    await userEvent.unhover(linha);
    expect(screen.queryByText('Diferença +1')).not.toBeInTheDocument();

    await userEvent.tab();
    expect(linha).toHaveFocus();
    expect(screen.getByText('Diferença +1')).toBeInTheDocument();
  });
});

describe('CelulaCalor', () => {
  const celula = (valor) => {
    const { container } = render(<table><tbody><tr><CelulaCalor valor={valor} /></tr></tbody></table>);
    return container.querySelector('td');
  };

  it('fica mais escura quanto maior a nota, com texto legível', () => {
    expect(celula(0)).toHaveStyle({ background: '#ecfdf5' });
    expect(celula(5)).toHaveStyle({ background: '#047857', color: '#ffffff' });
  });

  it('mostra um traço quando não há nota', () => {
    expect(celula(null)).toHaveTextContent('—');
  });
});
