import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(true);

  // O cookie de sessão é HttpOnly: a única forma de saber se há sessão é perguntar ao backend.
  useEffect(() => {
    api
      .get('/auth/me')
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCarregando(false));
  }, []);

  useEffect(() => {
    const sair = () => setUsuario(null);
    window.addEventListener('sdpp:sessao-expirada', sair);
    return () => window.removeEventListener('sdpp:sessao-expirada', sair);
  }, []);

  const entrar = useCallback(async (email, senha) => {
    const resp = await api.post('/auth/login', { email, senha });
    setUsuario(resp.usuario);
    return resp.usuario;
  }, []);

  const sair = useCallback(async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      setUsuario(null);
    }
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

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
