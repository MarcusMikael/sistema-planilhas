import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { AuthProvider, useAuth } from '../auth.jsx';
import { mockFetch, resposta } from '../test/fetch.js';
import Login from './Login.jsx';

function Tela() {
  const { usuario, carregando } = useAuth();
  if (carregando) return null;
  return usuario ? <p>Logado como {usuario.nome}</p> : <Login />;
}

const preencher = async (email, senha) => {
  await userEvent.type(await screen.findByLabelText('E-mail'), email);
  await userEvent.type(screen.getByLabelText('Senha'), senha);
  await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));
};

describe('Login', () => {
  it('entra com credenciais válidas usando o cookie de sessão', async () => {
    const fetch = mockFetch({
      'GET /auth/me': resposta(401, { mensagem: 'Autenticação necessária.' }),
      'POST /auth/login': resposta(200, { token: 'x', usuario: { nome: 'Ana Souza', perfil: 'RH' } }),
    });
    render(<AuthProvider><Tela /></AuthProvider>);
    await preencher('rh@sdpp.local', 'segredo');

    expect(await screen.findByText('Logado como Ana Souza')).toBeInTheDocument();
    const [, opcoes] = fetch.mock.calls.find(([url]) => url.endsWith('/auth/login'));
    expect(opcoes.credentials).toBe('include');
    expect(opcoes.headers['X-SDPP-CSRF']).toBe('1');
    expect(JSON.parse(opcoes.body)).toEqual({ email: 'rh@sdpp.local', senha: 'segredo' });
    // O token nunca é guardado em armazenamento acessível ao JavaScript
    expect(localStorage.length).toBe(0);
  });

  it('mostra a mensagem do servidor quando as credenciais são inválidas', async () => {
    mockFetch({
      'GET /auth/me': resposta(401, {}),
      'POST /auth/login': resposta(401, { mensagem: 'E-mail ou senha inválidos.' }),
    });
    render(<AuthProvider><Tela /></AuthProvider>);
    await preencher('rh@sdpp.local', 'errada');
    expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos.');
  });

  it('restaura a sessão existente sem pedir login', async () => {
    mockFetch({ 'GET /auth/me': resposta(200, { nome: 'Carlos Lima', perfil: 'GESTOR' }) });
    render(<AuthProvider><Tela /></AuthProvider>);
    expect(await screen.findByText('Logado como Carlos Lima')).toBeInTheDocument();
  });

  it('entra com um clique como um personagem da demonstração', async () => {
    const fetch = mockFetch({
      'GET /auth/me': resposta(401, {}),
      'POST /auth/login': resposta(200, { token: 'x', usuario: { nome: 'Maria Oliveira', perfil: 'COLABORADOR' } }),
    });
    render(<AuthProvider><Tela /></AuthProvider>);
    await userEvent.click(await screen.findByRole('button', { name: 'Entrar como Maria (Destaque)' }));

    expect(await screen.findByText('Logado como Maria Oliveira')).toBeInTheDocument();
    const [, opcoes] = fetch.mock.calls.find(([url]) => url.endsWith('/auth/login'));
    expect(JSON.parse(opcoes.body)).toEqual({ email: 'maria@sdpp.local', senha: 'sdpp123' });
  });
});
