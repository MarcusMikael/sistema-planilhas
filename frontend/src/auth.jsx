import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, getToken, setToken } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(Boolean(getToken()));

  useEffect(() => {
    if (!getToken()) return;
    api
      .get('/auth/me')
      .then(setUsuario)
      .catch(() => setToken(null))
      .finally(() => setCarregando(false));
  }, []);

  useEffect(() => {
    const sair = () => setUsuario(null);
    window.addEventListener('sdpp:sessao-expirada', sair);
    return () => window.removeEventListener('sdpp:sessao-expirada', sair);
  }, []);

  const entrar = useCallback(async (email, senha) => {
    const resp = await api.post('/auth/login', { email, senha });
    setToken(resp.token);
    setUsuario(resp.usuario);
    return resp.usuario;
  }, []);

  const sair = useCallback(() => {
    setToken(null);
    setUsuario(null);
  }, []);

  const valor = {
    usuario,
    carregando,
    entrar,
    sair,
    isRH: usuario?.perfil === 'RH',
    isGestor: usuario?.perfil === 'GESTOR',
    isColaborador: usuario?.perfil === 'COLABORADOR',
  };
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
