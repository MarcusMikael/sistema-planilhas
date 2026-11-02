import { useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao } from '../hooks.js';
import { Alerta, Campo, Cartao, Pagina } from '../components/ui.jsx';
import { ROTULOS } from '../format.js';

export default function Conta() {
  const { usuario } = useAuth();
  const [atual, setAtual] = useState('');
  const [nova, setNova] = useState('');
  const [confirma, setConfirma] = useState('');
  const [ok, setOk] = useState(false);
  const acao = useAcao();

  async function enviar(e) {
    e.preventDefault();
    setOk(false);
    if (nova !== confirma) {
      acao.setErro(new Error('A confirmação não confere com a nova senha.'));
      return;
    }
    try {
      await acao.executar(() => api.post('/auth/senha', { senhaAtual: atual, novaSenha: nova }));
      setOk(true);
      setAtual('');
      setNova('');
      setConfirma('');
    } catch {
      /* erro exibido pelo Alerta */
    }
  }

  return (
    <Pagina titulo="Minha conta">
      <div className="grade grade-2">
        <Cartao titulo="Dados de acesso">
          <p><strong>{usuario.nome}</strong></p>
          <p className="texto-2">{usuario.email}</p>
          <p>Perfil: {ROTULOS.perfil[usuario.perfil]}</p>
        </Cartao>
        <Cartao titulo="Trocar senha">
          <form className="form" onSubmit={enviar}>
            <Alerta erro={acao.erro} />
            {ok && <Alerta tipo="ok">Senha alterada com sucesso.</Alerta>}
            <Campo rotulo="Senha atual">
              <input type="password" value={atual} onChange={(e) => setAtual(e.target.value)} required autoComplete="current-password" />
            </Campo>
            <Campo rotulo="Nova senha" dica="Mínimo de 6 caracteres">
              <input type="password" value={nova} onChange={(e) => setNova(e.target.value)} required minLength={6} autoComplete="new-password" />
            </Campo>
            <Campo rotulo="Confirmar nova senha">
              <input type="password" value={confirma} onChange={(e) => setConfirma(e.target.value)} required autoComplete="new-password" />
            </Campo>
            <div className="form-acoes">
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        </Cartao>
      </div>
    </Pagina>
  );
}
