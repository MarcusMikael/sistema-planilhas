import { useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Seletor } from '../components/ui.jsx';
import { ROTULOS } from '../format.js';

const VAZIO = { nome: '', email: '', senha: '', perfil: 'COLABORADOR', ativo: true, colaboradorId: null };

export default function Usuarios() {
  const { usuario } = useAuth();
  const lista = useApi('/usuarios');
  const colabs = useApi('/colaboradores');
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(VAZIO);
  const acao = useAcao();

  function abrir(u) {
    acao.setErro(null);
    setEditando(u ?? {});
    setForm(u ? { nome: u.nome, email: u.email, senha: '', perfil: u.perfil, ativo: u.ativo, colaboradorId: u.colaboradorId } : VAZIO);
  }

  async function salvar(e) {
    e.preventDefault();
    const corpo = { ...form, senha: form.senha || null };
    try {
      await acao.executar(() => (editando.id ? api.put(`/usuarios/${editando.id}`, corpo) : api.post('/usuarios', corpo)));
      setEditando(null);
      lista.recarregar();
      colabs.recarregar();
    } catch {
      /* exibido */
    }
  }

  const set = (campo) => (v) => setForm((f) => ({ ...f, [campo]: v }));
  const vinculaveis = (colabs.data || []).filter((c) => !c.usuarioId || c.usuarioId === editando?.id);

  function escolherColaborador(id) {
    const c = (colabs.data || []).find((x) => String(x.id) === String(id));
    setForm((f) => ({ ...f, colaboradorId: id && Number(id), nome: f.nome || c?.nome || '', email: f.email || c?.email || '' }));
  }

  return (
    <Pagina titulo="Usuários e acessos" subtitulo="Contas de acesso, perfis (RH, Gestor, Colaborador) e vínculo com o cadastro"
      acoes={<button className="btn-primario" onClick={() => abrir(null)}>+ Novo usuário</button>}>
      <Cartao>
        <Alerta erro={lista.erro} />
        {lista.carregando ? <Carregando /> : (
          <div className="tabela-wrap">
            <table>
              <thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th>Colaborador vinculado</th><th>Situação</th><th /></tr></thead>
              <tbody>
                {(lista.data || []).map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.nome}</strong></td>
                    <td>{u.email}</td>
                    <td>{ROTULOS.perfil[u.perfil]}</td>
                    <td>{u.colaboradorNome ?? <span className="texto-2">—</span>}</td>
                    <td><Badge status={u.ativo ? 'ATIVO' : 'CANCELADO'}>{u.ativo ? 'Ativo' : 'Inativo'}</Badge></td>
                    <td className="acoes">
                      <button className="btn-texto" onClick={() => abrir(u)}>Editar</button>
                      {u.ativo && u.id !== usuario.id && (
                        <button className="btn-texto btn-perigo" onClick={async () => {
                          if (window.confirm(`Desativar o acesso de ${u.nome}?`)) {
                            await api.del(`/usuarios/${u.id}`);
                            lista.recarregar();
                          }
                        }}>Desativar</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Cartao>

      <Modal titulo={editando?.id ? 'Editar usuário' : 'Novo usuário'} aberto={editando !== null} onFechar={() => setEditando(null)}>
        <form className="form" onSubmit={salvar}>
          <Alerta erro={acao.erro} />
          <Campo rotulo="Colaborador vinculado" dica="Necessário para avaliar, responder clima e ter PDI">
            <Seletor valor={form.colaboradorId} onChange={escolherColaborador} vazio="Nenhum"
              opcoes={vinculaveis.map((c) => ({ valor: c.id, rotulo: `${c.nome} — ${c.departamentoNome}` }))} />
          </Campo>
          <Campo rotulo="Nome"><input value={form.nome} onChange={(e) => set('nome')(e.target.value)} required /></Campo>
          <Campo rotulo="E-mail de acesso"><input type="email" value={form.email} onChange={(e) => set('email')(e.target.value)} required /></Campo>
          <div className="grade grade-2">
            <Campo rotulo="Perfil">
              <Seletor valor={form.perfil} onChange={set('perfil')} disabled={editando?.id === usuario.id}
                opcoes={Object.entries(ROTULOS.perfil).map(([valor, rotulo]) => ({ valor, rotulo }))} />
            </Campo>
            <Campo rotulo={editando?.id ? 'Nova senha (opcional)' : 'Senha inicial'} dica="Mínimo de 6 caracteres">
              <input type="password" value={form.senha} minLength={6} required={!editando?.id} autoComplete="new-password"
                onChange={(e) => set('senha')(e.target.value)} />
            </Campo>
          </div>
          {editando?.id && editando.id !== usuario.id && (
            <label className="check"><input type="checkbox" checked={form.ativo} onChange={(e) => set('ativo')(e.target.checked)} /> Ativo</label>
          )}
          <div className="form-acoes">
            <button type="button" onClick={() => setEditando(null)}>Cancelar</button>
            <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
          </div>
        </form>
      </Modal>
    </Pagina>
  );
}
