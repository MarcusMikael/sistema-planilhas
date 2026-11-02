// Cliente da API REST do SDPP. O token JWT fica no localStorage e vai no cabeçalho Authorization.
const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');
const CHAVE_TOKEN = 'sdpp.token';

export class ApiError extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem);
    this.status = status;
    this.campos = campos || null;
  }
}

export function getToken() {
  try {
    return localStorage.getItem(CHAVE_TOKEN);
  } catch {
    return null;
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(CHAVE_TOKEN, token);
    else localStorage.removeItem(CHAVE_TOKEN);
  } catch {
    /* armazenamento indisponível */
  }
}

async function request(method, path, body) {
  const headers = { Accept: 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let resp;
  try {
    resp = await fetch(`${BASE}/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se o backend está em execução.');
  }

  if (resp.status === 204) return null;
  const texto = await resp.text();
  const dados = texto ? JSON.parse(texto) : null;
  if (!resp.ok) {
    if (resp.status === 401 && path !== '/auth/login') {
      setToken(null);
      window.dispatchEvent(new Event('sdpp:sessao-expirada'));
    }
    throw new ApiError(resp.status, dados?.mensagem || `Erro ${resp.status}`, dados?.campos);
  }
  return dados;
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body ?? {}),
  put: (path, body) => request('PUT', path, body),
  patch: (path, body) => request('PATCH', path, body),
  del: (path) => request('DELETE', path),
};
