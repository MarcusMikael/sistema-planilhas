import { describe, expect, it } from 'vitest';
import { tomStatus } from './format.js';

describe('tomStatus', () => {
  it.each([
    ['CONCLUIDA', 'ok'],
    ['ATIVO', 'ok'],
    ['RESULTADOS_LIBERADOS', 'ok'],
    ['EM_ANDAMENTO', 'aviso'],
    ['PENDENTE', 'aviso'],
    ['LIBERADO', 'info'],
    ['ERRO', 'erro'],
    ['RASCUNHO', 'neutro'],
    ['A_FAZER', 'neutro'],
    ['ENCERRADA', 'neutro'],
  ])('%s usa o tom %s', (status, tom) => {
    expect(tomStatus(status)).toBe(tom);
  });
});
