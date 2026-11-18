import { useState } from 'react';
import { Check, TrendingUp } from 'lucide-react';
import { useAuth } from '../auth.jsx';
import { Alerta, Campo } from '../components/ui.jsx';
import { useTituloAba } from '../hooks.js';

// Contas FICTÍCIAS da carga de demonstração (ver README), com a senha pública das contas de demonstração.
// Entram com um clique, para facilitar demonstrações. Quem publicar uma versão para uso real deve desativar
// essas contas no servidor e definir VITE_OCULTAR_CONTAS_DEMO=true para esconder os atalhos.
const SENHA_DEMO = 'sdpp123';
const CONTAS_DEMO = [
  { nome: 'Ana', papel: 'RH', email: 'rh@sdpp.local', perfil: 'rh' },
  { nome: 'Carlos', papel: 'Gestor', email: 'gestor@sdpp.local', perfil: 'gestor' },
  { nome: 'Maria', papel: 'Destaque', email: 'maria@sdpp.local', perfil: 'colaborador' },
  { nome: 'Pedro', papel: 'Constante', email: 'pedro@sdpp.local', perfil: 'colaborador' },
  { nome: 'João', papel: 'Com PDI', email: 'joao@sdpp.local', perfil: 'colaborador' },
  { nome: 'Beatriz', papel: 'Novata', email: 'beatriz@sdpp.local', perfil: 'colaborador' },
];
const MOSTRAR_CONTAS_DEMO = import.meta.env.VITE_OCULTAR_CONTAS_DEMO !== 'true';

const RECURSOS = [
  'Autoavaliação e avaliação do gestor por competências',
  'Resultados liberados pelo RH e comparação lado a lado',
  'Planos de desenvolvimento acompanhados pelo gestor',
  'Pesquisa de clima com respostas anônimas',
];

// Ilustração decorativa da Matriz Nine Box: quantas "pessoas" aparecem em cada quadrante
// (linhas de cima para baixo = potencial alto → baixo; colunas = desempenho baixo → alto).
const NINE_BOX = [1, 2, 3, 1, 3, 2, 0, 1, 1];

function IlustracaoNineBox() {
  return (
    <div className="login-ninebox" aria-hidden="true">
      <span className="login-ninebox-eixo login-ninebox-eixo-y">Potencial</span>
      <div className="login-ninebox-grade">
        {NINE_BOX.map((qtd, i) => (
          <div key={i} className={`login-ninebox-celula${i === 2 ? ' destaque' : ''}`}>
            {Array.from({ length: qtd }, (_, j) => <span key={j} className="login-ninebox-ponto" />)}
          </div>
        ))}
      </div>
      <span className="login-ninebox-eixo login-ninebox-eixo-x">Desempenho</span>
    </div>
  );
}

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
        <div className="login-marca">
          <span className="marca-logo" aria-hidden="true"><TrendingUp size={18} strokeWidth={1.75} /></span>
          <strong>SDPP</strong>
        </div>
        <div className="login-lado-conteudo">
          <h1>Sistema de Desenvolvimento e Performance de Pessoas</h1>
          <p>Avaliação de desempenho, Matriz Nine Box, PDI e pesquisa de clima em um só lugar.</p>
          <ul>
            {RECURSOS.map((r) => (
              <li key={r}><span className="login-check" aria-hidden="true"><Check size={13} strokeWidth={2.5} /></span>{r}</li>
            ))}
          </ul>
          <IlustracaoNineBox />
        </div>
      </div>
      <div className="login-form">
        <div className="cartao">
          <form className="form" onSubmit={enviar}>
            <div className="login-cabecalho">
              <h2>Acesse sua conta</h2>
              <p>Use seu e-mail e senha para entrar.</p>
            </div>
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
                      <span className={`avatar-demo avatar-${c.perfil}`} aria-hidden="true">{c.nome[0]}</span>
                      <span className="conta-demo-texto">
                        <strong>{c.nome}</strong>
                        <span>{c.papel}</span>
                      </span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </form>
        </div>
        <p className="login-rodape">Projeto acadêmico · Liderança e Gestão de Pessoas · UNIFSA 2026.2</p>
      </div>
    </div>
  );
}
