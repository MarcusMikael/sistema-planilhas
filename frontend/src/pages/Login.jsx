import { useState } from 'react';
import { useAuth } from '../auth.jsx';
import { Alerta, Campo } from '../components/ui.jsx';
import { useTituloAba } from '../hooks.js';

// Contas FICTÍCIAS da carga de demonstração (ver README), com a senha pública das contas de demonstração.
// Entram com um clique, para facilitar demonstrações. Quem publicar uma versão para uso real deve desativar
// essas contas no servidor e definir VITE_OCULTAR_CONTAS_DEMO=true para esconder os atalhos.
const SENHA_DEMO = 'sdpp123';
const CONTAS_DEMO = [
  { nome: 'Ana', papel: 'RH', email: 'rh@sdpp.local' },
  { nome: 'Carlos', papel: 'Gestor', email: 'gestor@sdpp.local' },
  { nome: 'Maria', papel: 'Destaque', email: 'maria@sdpp.local' },
  { nome: 'Pedro', papel: 'Constante', email: 'pedro@sdpp.local' },
  { nome: 'João', papel: 'Com PDI', email: 'joao@sdpp.local' },
  { nome: 'Beatriz', papel: 'Novata', email: 'beatriz@sdpp.local' },
];
const MOSTRAR_CONTAS_DEMO = import.meta.env.VITE_OCULTAR_CONTAS_DEMO !== 'true';

export default function Login() {
  useTituloAba('Entrar');
  const { entrar } = useAuth();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  function enviar(e) {
    e.preventDefault();
    return entrarCom(email, senha);
  }

  async function entrarCom(emailLogin, senhaLogin) {
    setErro(null);
    setEnviando(true);
    try {
      await entrar(emailLogin, senhaLogin);
    } catch (ex) {
      setErro(ex);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login">
      <div className="login-lado">
        <h1>Sistema de Desenvolvimento e Performance de Pessoas</h1>
        <p>Avaliação de desempenho, Matriz Nine Box, PDI e pesquisa de clima em um só lugar.</p>
        <ul>
          <li>Autoavaliação e avaliação do gestor por competências</li>
          <li>Resultados liberados pelo RH e comparação lado a lado</li>
          <li>Planos de desenvolvimento acompanhados pelo gestor</li>
          <li>Pesquisa de clima com respostas anônimas</li>
        </ul>
      </div>
      <div className="login-form">
        <div className="cartao">
          <form className="form" onSubmit={enviar}>
            <h2>Entrar</h2>
            <Alerta erro={erro} />
            <Campo rotulo="E-mail">
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus
                autoComplete="username" />
            </Campo>
            <Campo rotulo="Senha">
              <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required
                autoComplete="current-password" />
            </Campo>
            <button className="btn-primario" type="submit" disabled={enviando}>
              {enviando ? 'Entrando…' : 'Entrar'}
            </button>
            {MOSTRAR_CONTAS_DEMO && (
              <div className="contas-demo">
                <p>Demonstração: entre com uma das contas (dados fictícios)</p>
                <div className="contas-demo-grade">
                  {CONTAS_DEMO.map((c) => (
                    <button type="button" key={c.email} disabled={enviando}
                      aria-label={`Entrar como ${c.nome} (${c.papel})`}
                      onClick={() => entrarCom(c.email, SENHA_DEMO)}>
                      <strong>{c.nome}</strong>
                      <span>{c.papel}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}
