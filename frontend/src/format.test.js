import { describe, expect, it } from 'vitest';
import { SENHA_PADRAO, tomStatus } from './format.js';

describe('SENHA_PADRAO', () => {
  // O navegador aplica o atributo pattern ao valor inteiro, com a flag "v"
  const valida = (s) => new RegExp(`^(?:${SENHA_PADRAO})$`, 'v').test(s);

  it.each(['Senha123', 'abc12345', 'Ação2026x', 'sdpp-2026'])('aceita %s', (s) => {
    expect(valida(s)).toBe(true);
  });

  it.each(['abcdefgh', '12345678', 'abc123', 'ddddddd1'.slice(0, 7)])('recusa %s', (s) => {
    expect(valida(s)).toBe(false);
  });
});

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
