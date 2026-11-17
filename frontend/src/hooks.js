import { useCallback, useEffect, useId, useState } from 'react';
import { useBlocker } from 'react-router-dom';
import { api } from './api.js';

/**
 * Carrega um recurso da API; path nulo não faz requisição.
 * Só a resposta da requisição mais recente é aplicada: se o path mudar (ex.: troca de ciclo no seletor),
 * a requisição anterior é cancelada e uma resposta atrasada não sobrescreve a atual.
 */
export function useApi(path) {
  // Resultado da última requisição concluída, marcado com o path e a versão que o produziram.
  const [resultado, setResultado] = useState({ path: null, versao: -1, data: null, erro: null });
  const [versao, setVersao] = useState(0);

  useEffect(() => {
    if (!path) return undefined;
    const controle = new AbortController();
    // Uma resposta que chega depois do cancelamento (troca de path, recarga, desmontagem) é descartada
    api.get(path, { signal: controle.signal }).then(
      (data) => {
        if (!controle.signal.aborted) setResultado({ path, versao, data, erro: null });
      },
      (erro) => {
        if (!controle.signal.aborted) setResultado({ path, versao, data: null, erro });
      },
    );
    // Troca de path, recarga ou desmontagem: cancela a requisição anterior
    return () => controle.abort();
  }, [path, versao]);

  const atual = Boolean(path) && resultado.path === path;
  const recarregar = useCallback(() => setVersao((v) => v + 1), []);
  const setData = useCallback(
    (novo) => setResultado((r) => ({ ...r, data: typeof novo === 'function' ? novo(r.data) : novo })),
    [],
  );

  return {
    data: atual ? resultado.data : null,
    erro: atual ? resultado.erro : null,
    carregando: Boolean(path) && (!atual || resultado.versao !== versao),
    recarregar,
    setData,
  };
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

/** Título da aba do navegador: "Tela · SDPP" (ajuda no histórico, nas abas e em leitores de tela). */
export function useTituloAba(titulo) {
  useEffect(() => {
    if (typeof titulo === 'string' && titulo) document.title = `${titulo} · SDPP`;
  }, [titulo]);
}

export const MSG_ALTERACOES = 'Há alterações não salvas. Sair mesmo assim e descartá-las?';

// Telas e modais com alterações pendentes (id → mensagem). O React Router consulta só um bloqueador
// por vez, então as telas apenas se registram aqui e um único useBlocker (BloqueioSaida) decide.
const pendentes = new Map();

/** Marca a tela atual como tendo alterações não salvas enquanto `sujo` for verdadeiro. */
export function useAvisoSaida(sujo, mensagem = MSG_ALTERACOES) {
  const id = useId();
  useEffect(() => {
    if (!sujo) return undefined;
    pendentes.set(id, mensagem);
    return () => pendentes.delete(id);
  }, [id, sujo, mensagem]);
}

/**
 * Pede confirmação antes de sair com alterações não salvas: navegação interna (links, menu, voltar)
 * e fechar/recarregar a aba. Deve ser montado uma única vez (no Layout).
 */
export function useBloqueioSaida() {
  const blocker = useBlocker(({ currentLocation, nextLocation }) =>
    pendentes.size > 0 && currentLocation.pathname !== nextLocation.pathname);

  useEffect(() => {
    if (blocker.state !== 'blocked') return;
    const mensagem = pendentes.values().next().value ?? MSG_ALTERACOES;
    if (window.confirm(mensagem)) {
      pendentes.clear();
      blocker.proceed();
    } else {
      blocker.reset();
    }
  }, [blocker]);

  useEffect(() => {
    const aviso = (e) => {
      if (pendentes.size === 0) return;
      e.preventDefault();
      e.returnValue = '';
    };
    window.addEventListener('beforeunload', aviso);
    return () => window.removeEventListener('beforeunload', aviso);
  }, []);
}
