import { useState } from 'react';
import { useAuth } from '../auth.jsx';
import { Alerta, Campo } from '../components/ui.jsx';

// Contas FICTÍCIAS da carga de demonstração (ver README). Só aparecem em modo de desenvolvimento.
const CONTAS_DEMO = [
  { rotulo: 'RH', email: 'rh@sdpp.local' },
  { rotulo: 'Gestor', email: 'gestor@sdpp.local' },
  { rotulo: 'Colaboradora', email: 'marina@sdpp.local' },
];

export default function Login() {
  const { entrar } = useAuth();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(e) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await entrar(email, senha);
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
            {import.meta.env.DEV && (
              <div className="contas-demo">
                <p>Contas de demonstração (dados fictícios, senha no README):</p>
                <div className="linha">
                  {CONTAS_DEMO.map((c) => (
                    <button type="button" key={c.email} onClick={() => setEmail(c.email)}>
                      {c.rotulo}
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
