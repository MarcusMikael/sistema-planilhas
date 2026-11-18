import { useState } from 'react';
import { ClipboardCheck, LayoutGrid, MessageSquareHeart, Sprout, TrendingUp } from 'lucide-react';
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

// Módulos do sistema, com os mesmos ícones do menu lateral
const MODULOS = [
  { Icone: ClipboardCheck, titulo: 'Avaliação de desempenho', texto: 'Autoavaliação e avaliação do gestor por competências.' },
  { Icone: LayoutGrid, titulo: 'Matriz Nine Box', texto: 'Desempenho e potencial de cada pessoa em uma matriz 3×3.' },
  { Icone: Sprout, titulo: 'Plano de desenvolvimento', texto: 'Ações com prazo, acompanhadas pelo gestor.' },
  { Icone: MessageSquareHeart, titulo: 'Pesquisa de clima', texto: 'Respostas anônimas, resultados por setor.' },
];

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
        <div className="marca">
          <span className="marca-logo" aria-hidden="true"><TrendingUp size={17} strokeWidth={1.5} /></span>
          <strong>SDPP</strong>
        </div>
        <div className="login-lado-conteudo">
          <h1>Sistema de Desenvolvimento e Performance de Pessoas</h1>
          <p>Avaliação de desempenho, Matriz Nine Box, PDI e pesquisa de clima em um só lugar.</p>
          <ul className="login-modulos">
            {MODULOS.map(({ Icone, titulo, texto }) => (
              <li key={titulo}>
                <span className="login-modulo-icone" aria-hidden="true"><Icone size={18} strokeWidth={1.5} /></span>
                <span>
                  <strong>{titulo}</strong>
                  <span>{texto}</span>
                </span>
              </li>
            ))}
          </ul>
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
