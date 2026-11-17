// Cliente da API REST do SDPP.
// A sessão fica em um cookie HttpOnly definido pelo backend: o JavaScript da página não tem acesso ao token,
// então um eventual XSS não consegue roubá-lo. Em produção a Vercel repassa /api ao backend (mesmo domínio).
const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

// Cabeçalho exigido pelo backend em requisições que alteram dados (proteção contra CSRF):
// um site de terceiros não consegue enviá-lo sem passar pelo CORS.
const CABECALHO_CSRF = { 'X-SDPP-CSRF': '1' };

// Versões anteriores guardavam o token no localStorage; remove o resíduo.
try {
  localStorage.removeItem('sdpp.token');
} catch {
  /* armazenamento indisponível */
}

// ---------- Requisições lentas ----------
// No plano gratuito do Render o backend "dorme" sem uso e o primeiro acesso pode levar até 1 minuto.
// Se alguma requisição passar de LIMITE_LENTO_MS, a interface mostra um aviso (AvisoServidorLento).
export const LIMITE_LENTO_MS = 3000;
let lentas = 0;
const ouvintesLentidao = new Set();

function mudarLentas(delta) {
  lentas += delta;
  ouvintesLentidao.forEach((f) => f());
}

/** Há alguma requisição em andamento há mais de LIMITE_LENTO_MS? */
export function servidorLento() {
  return lentas > 0;
}

/** Inscreve uma função chamada quando servidorLento() muda; devolve a função para cancelar. */
export function observarLentidao(ouvinte) {
  ouvintesLentidao.add(ouvinte);
  return () => ouvintesLentidao.delete(ouvinte);
}

export class ApiError extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem);
    this.status = status;
    this.campos = campos || null;
  }
}

async function request(method, path, body, opcoes = {}) {
  let lenta = false;
  const timer = setTimeout(() => {
    lenta = true;
    mudarLentas(1);
  }, LIMITE_LENTO_MS);
  try {
    return await executar(method, path, body, opcoes);
  } finally {
    clearTimeout(timer);
    if (lenta) mudarLentas(-1);
  }
}

async function executar(method, path, body, { signal } = {}) {
  const headers = { Accept: 'application/json', ...CABECALHO_CSRF };
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let resp;
  try {
    resp = await fetch(`${BASE}/api${path}`, {
      method,
      headers,
      credentials: 'include',
      signal,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (e) {
    if (e.name === 'AbortError') throw e;
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se o backend está em execução.');
  }

  if (resp.status === 204) return null;
  const texto = await resp.text();
  let dados = null;
  try {
    dados = texto ? JSON.parse(texto) : null;
  } catch {
    // Resposta que não é JSON (ex.: página de erro 502 do proxy)
    if (resp.ok) throw new ApiError(resp.status, 'Resposta inválida do servidor.');
  }
  if (!resp.ok) {
    if (resp.status === 401 && path !== '/auth/login' && path !== '/auth/me') {
      window.dispatchEvent(new Event('sdpp:sessao-expirada'));
    }
    throw new ApiError(resp.status, dados?.mensagem || `Erro ${resp.status}`, dados?.campos);
  }
  return dados;
}

export const api = {
  get: (path, opcoes) => request('GET', path, undefined, opcoes),
  post: (path, body) => request('POST', path, body ?? {}),
  put: (path, body) => request('PUT', path, body),
  patch: (path, body) => request('PATCH', path, body),
  del: (path) => request('DELETE', path),
};
