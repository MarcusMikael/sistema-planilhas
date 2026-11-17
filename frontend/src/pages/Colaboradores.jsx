import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Seletor, Vazio } from '../components/ui.jsx';
import { ROTULOS, SENHA_DICA, SENHA_PADRAO, fmtData, hojeIso } from '../format.js';

const VAZIO = { nome: '', email: '', cargoId: null, departamentoId: null, gestorId: null, dataAdmissao: '', ativo: true };

export default function Colaboradores() {
  const { isRH, usuario } = useAuth();
  const lista = useApi('/colaboradores');
  const cargos = useApi(isRH ? '/cargos' : null);
  const depts = useApi(isRH ? '/departamentos' : null);
  const [busca, setBusca] = useState('');
  const [filtroDept, setFiltroDept] = useState(null);
  const [mostrarInativos, setMostrarInativos] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(VAZIO);
  const [acesso, setAcesso] = useState({ criar: false, perfil: 'COLABORADOR', senha: '' });
  const acao = useAcao();
  const acaoLista = useAcao();

  const filtrados = useMemo(() => (lista.data || [])
    .filter((c) => isRH || c.id !== usuario.colaboradorId)
    .filter((c) => mostrarInativos || c.ativo)
    .filter((c) => !filtroDept || String(c.departamentoId) === String(filtroDept))
    .filter((c) => !busca || `${c.nome} ${c.email} ${c.cargoNome}`.toLowerCase().includes(busca.toLowerCase())),
  [lista.data, busca, filtroDept, mostrarInativos, isRH, usuario.colaboradorId]);

  function abrir(c) {
    acao.setErro(null);
    setEditando(c ?? {});
    setForm(c ? { nome: c.nome, email: c.email, cargoId: c.cargoId, departamentoId: c.departamentoId,
      gestorId: c.gestorId, dataAdmissao: c.dataAdmissao, ativo: c.ativo } : VAZIO);
    setAcesso({ criar: false, perfil: 'COLABORADOR', senha: '' });
  }

  async function salvar(e) {
    e.preventDefault();
    let alterou = false;
    try {
      await acao.executar(async () => {
        const salvo = editando.id
          ? await api.put(`/colaboradores/${editando.id}`, form)
          : await api.post('/colaboradores', form);
        alterou = true;
        // A partir daqui o colaborador já existe: se a criação do acesso falhar, o formulário passa a editar
        // esse cadastro (em vez de tentar criá-lo de novo e esbarrar no e-mail repetido)
        setEditando(salvo);
        if (acesso.criar) {
          await api.post('/usuarios', { nome: salvo.nome, email: salvo.email, senha: acesso.senha,
            perfil: acesso.perfil, ativo: true, colaboradorId: salvo.id });
        }
      });
      setEditando(null);
    } catch {
      /* exibido no formulário */
    } finally {
      if (alterou) lista.recarregar();
    }
  }

  async function desativar(c) {
    if (!window.confirm(`Desativar ${c.nome}? O histórico será preservado e o acesso, bloqueado.`)) return;
    try {
      await acaoLista.executar(() => api.del(`/colaboradores/${c.id}`));
      lista.recarregar();
    } catch {
      /* exibido acima da lista */
    }
  }

  const set = (campo) => (v) => setForm((f) => ({ ...f, [campo]: v }));
  // Só quem tem acesso de Gestor ou RH pode gerir uma equipe (o atual continua na lista, se for o caso)
  const gestores = (lista.data || []).filter((c) => c.id !== editando?.id
    && (c.id === form.gestorId || (c.ativo && (c.perfil === 'GESTOR' || c.perfil === 'RH'))));

  return (
    <Pagina
      titulo={isRH ? 'Colaboradores' : 'Minha equipe'}
      subtitulo={isRH ? 'Cadastro com cargo, departamento, gestor responsável e data de admissão'
        : 'Colaboradores sob sua gestão'}
      acoes={isRH && <button className="btn-primario" onClick={() => abrir(null)}>+ Novo colaborador</button>}
    >
      <Cartao>
        <div className="filtros" style={{ marginBottom: 12 }}>
          <Campo rotulo="Buscar"><input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Nome, e-mail ou cargo" /></Campo>
          {isRH && (
            <Campo rotulo="Departamento">
              <Seletor valor={filtroDept} onChange={setFiltroDept} vazio="Todos"
                opcoes={(depts.data || []).map((d) => ({ valor: d.id, rotulo: d.nome }))} />
            </Campo>
          )}
          <label className="check"><input type="checkbox" checked={mostrarInativos}
            onChange={(e) => setMostrarInativos(e.target.checked)} /> Mostrar inativos</label>
        </div>
        <Alerta erro={lista.erro || acaoLista.erro} />
        {lista.carregando ? <Carregando /> : filtrados.length === 0 ? <Vazio>Nenhum colaborador encontrado.</Vazio> : (
          <div className="tabela-wrap">
            <table>
              <thead><tr><th>Nome</th><th>Cargo</th><th>Departamento</th><th>Gestor</th><th>Admissão</th><th>Acesso</th><th /></tr></thead>
              <tbody>
                {filtrados.map((c) => (
                  <tr key={c.id}>
                    <td><strong>{c.nome}</strong><div className="texto-2 pequeno">{c.email}</div></td>
                    <td>{c.cargoNome}</td>
                    <td>{c.departamentoNome}</td>
                    <td>{c.gestorNome ?? '—'}</td>
                    <td>{fmtData(c.dataAdmissao)}</td>
                    <td>{!c.ativo ? <Badge status="CANCELADO">Inativo</Badge>
                      : c.perfil ? <Badge status="ATIVO">{ROTULOS.perfil[c.perfil]}</Badge>
                        : <Badge status="PENDENTE">Sem acesso</Badge>}</td>
                    <td className="acoes">
                      <Link className="btn-texto" to={`/historico?colaborador=${c.id}`}>Histórico</Link>
                      {c.id !== usuario.colaboradorId && !isRH && <Link className="btn-texto" to={`/pdis?colaborador=${c.id}`}>PDI</Link>}
                      {isRH && <button className="btn-texto" onClick={() => abrir(c)}>Editar</button>}
                      {isRH && c.ativo && <button className="btn-texto btn-perigo" onClick={() => desativar(c)}>Desativar</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Cartao>

      <Modal titulo={editando?.id ? 'Editar colaborador' : 'Novo colaborador'} aberto={editando !== null} onFechar={() => setEditando(null)}>
        <form className="form" onSubmit={salvar}>
          <Alerta erro={acao.erro} />
          <Campo rotulo="Nome completo"><input value={form.nome} onChange={(e) => set('nome')(e.target.value)} required /></Campo>
          <Campo rotulo="E-mail"><input type="email" value={form.email} onChange={(e) => set('email')(e.target.value)} required /></Campo>
          <div className="grade grade-2">
            <Campo rotulo="Cargo atual">
              <Seletor valor={form.cargoId} onChange={(v) => set('cargoId')(v && Number(v))} vazio="Selecione" required
                opcoes={(cargos.data || []).map((c) => ({ valor: c.id, rotulo: c.nome }))} />
            </Campo>
            <Campo rotulo="Departamento">
              <Seletor valor={form.departamentoId} onChange={(v) => set('departamentoId')(v && Number(v))} vazio="Selecione" required
                opcoes={(depts.data || []).map((d) => ({ valor: d.id, rotulo: d.nome }))} />
            </Campo>
          </div>
          <div className="grade grade-2">
            <Campo rotulo="Gestor responsável" dica="Define a equipe; só aparecem pessoas com acesso de Gestor ou RH">
              <Seletor valor={form.gestorId} onChange={(v) => set('gestorId')(v && Number(v))} vazio="Sem gestor"
                opcoes={gestores.map((g) => ({ valor: g.id, rotulo: `${g.nome} (${g.cargoNome})` }))} />
            </Campo>
            <Campo rotulo="Data de admissão">
              <input type="date" value={form.dataAdmissao || ''} max={hojeIso()} onChange={(e) => set('dataAdmissao')(e.target.value)} required />
            </Campo>
          </div>
          {editando?.id && (
            <label className="check"><input type="checkbox" checked={form.ativo} onChange={(e) => set('ativo')(e.target.checked)} /> Ativo</label>
          )}
          {!editando?.usuarioId && (
            <div className="cartao" style={{ background: 'var(--superficie-2)', boxShadow: 'none' }}>
              <label className="check"><input type="checkbox" checked={acesso.criar}
                onChange={(e) => setAcesso((a) => ({ ...a, criar: e.target.checked }))} /> Criar acesso ao sistema</label>
              {acesso.criar && (
                <div className="grade grade-2" style={{ marginTop: 10 }}>
                  <Campo rotulo="Perfil">
                    <Seletor valor={acesso.perfil} onChange={(v) => setAcesso((a) => ({ ...a, perfil: v }))}
                      opcoes={Object.entries(ROTULOS.perfil).map(([valor, rotulo]) => ({ valor, rotulo }))} />
                  </Campo>
                  <Campo rotulo="Senha inicial" dica={SENHA_DICA}>
                    <input type="password" value={acesso.senha} minLength={8} pattern={SENHA_PADRAO} title={SENHA_DICA} required autoComplete="new-password"
                      onChange={(e) => setAcesso((a) => ({ ...a, senha: e.target.value }))} />
                  </Campo>
                </div>
              )}
            </div>
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
