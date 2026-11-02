import { useCallback, useEffect, useState } from 'react';
import { api } from './api.js';

/** Carrega um recurso da API; path nulo não faz requisição. */
export function useApi(path) {
  const [data, setData] = useState(null);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(Boolean(path));

  const recarregar = useCallback(async () => {
    if (!path) {
      setData(null);
      setCarregando(false);
      return;
    }
    setCarregando(true);
    setErro(null);
    try {
      setData(await api.get(path));
    } catch (e) {
      setErro(e);
      setData(null);
    } finally {
      setCarregando(false);
    }
  }, [path]);

  useEffect(() => {
    recarregar();
  }, [recarregar]);

  return { data, erro, carregando, recarregar, setData };
}

/** Executa uma ação (POST/PUT/DELETE) controlando estado de envio e erro. */
export function useAcao() {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState(null);

  const executar = useCallback(async (fn) => {
    setEnviando(true);
    setErro(null);
    try {
      return await fn();
    } catch (e) {
      setErro(e);
      throw e;
    } finally {
      setEnviando(false);
    }
  }, []);

  return { enviando, erro, setErro, executar };
}
