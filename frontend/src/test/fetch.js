import { vi } from 'vitest';

/** Resposta HTTP simulada com corpo JSON. */
export function resposta(status, corpo) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: async () => (corpo === undefined ? '' : JSON.stringify(corpo)),
  };
}

/**
 * Substitui o fetch global. `rotas` mapeia "MÉTODO /caminho" (sem o prefixo /api) para uma resposta
 * ou para uma função que recebe as opções da requisição e devolve a resposta (ou uma Promise dela).
 */
export function mockFetch(rotas) {
  const fn = vi.fn(async (url, opcoes = {}) => {
    const caminho = url.replace(/^.*\/api/, '');
    const chave = `${opcoes.method || 'GET'} ${caminho}`;
    const r = rotas[chave];
    if (!r) throw new Error(`Rota não simulada: ${chave}`);
    return typeof r === 'function' ? r(opcoes) : r;
  });
  vi.stubGlobal('fetch', fn);
  return fn;
}
