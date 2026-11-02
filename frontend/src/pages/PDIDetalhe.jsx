import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Progresso, Seletor, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtData } from '../format.js';
import { FormPDI } from './PDIs.jsx';

const ACAO_VAZIA = { descricao: '', dataInicio: '', dataTermino: '', entrega: '', recursos: '', status: 'A_FAZER', progresso: 0 };
const opcoesStatus = Object.entries(ROTULOS.statusAcao).map(([valor, rotulo]) => ({ valor, rotulo }));

function Lista({ texto }) {
  const itens = (texto || '').split('\n').map((s) => s.trim()).filter(Boolean);
  if (itens.length === 0) return <span className="texto-2">—</span>;
  return <ul style={{ margin: 0, paddingLeft: 18 }}>{itens.map((i) => <li key={i}>{i}</li>)}</ul>;
}

export default function PDIDetalhe() {
  const { id } = useParams();
  const { usuario, isRH } = useAuth();
  const pdi = useApi(`/pdis/${id}`);
  const ciclos = useApi('/ciclos');
  const [editPdi, setEditPdi] = useState(null);
  const [editAcao, setEditAcao] = useState(null);
  const [progresso, setProgresso] = useState(null);
  const acao = useAcao();
  const navegar = useNavigate();

  if (pdi.carregando) return <Carregando />;
  if (pdi.erro) return <Pagina titulo="PDI"><Alerta erro={pdi.erro} /></Pagina>;
  const p = pdi.data;
  const ehDono = p.colaboradorId === usuario.colaboradorId;
  const podeEditar = isRH || (p.gestorId != null && p.gestorId === usuario.colaboradorId && !ehDono);
  const ativo = p.status === 'ATIVO';

  async function exec(fn, fechar) {
    try {
      await acao.executar(fn);
      fechar?.();
      pdi.recarregar();
    } catch {
      /* exibido */
    }
  }

  const salvarPdi = (e) => {
    e.preventDefault();
    exec(() => api.put(`/pdis/${id}`, editPdi), () => setEditPdi(null));
  };
  const salvarAcao = (e) => {
    e.preventDefault();
    const corpo = { ...editAcao, progresso: Number(editAcao.progresso) };
    exec(() => (editAcao.id ? api.put(`/acoes-pdi/${editAcao.id}`, corpo) : api.post(`/pdis/${id}/acoes`, corpo)), () => setEditAcao(null));
  };
  const salvarProgresso = (e) => {
    e.preventDefault();
    exec(() => api.patch(`/acoes-pdi/${progresso.id}/progresso`, { status: progresso.status, progresso: Number(progresso.progresso) }),
      () => setProgresso(null));
  };

  return (
    <Pagina titulo={`PDI — ${p.colaboradorNome}`} subtitulo={p.objetivo}
      acoes={<>
        <Badge status={p.status} grupo="statusPDI" />
        {podeEditar && <button onClick={() => { acao.setErro(null); setEditPdi({ colaboradorId: p.colaboradorId, cicloId: p.cicloId,
          cargoDesejado: p.cargoDesejado || '', objetivo: p.objetivo, habilidadesDesenvolver: p.habilidadesDesenvolver || '',
          habilidadesSucesso: p.habilidadesSucesso || '', anotacoes: p.anotacoes || '', status: p.status }); }}>Editar PDI</button>}
        {podeEditar && <button className="btn-perigo" onClick={() => {
          if (window.confirm('Excluir este PDI e todas as suas ações?')) exec(() => api.del(`/pdis/${id}`), () => navegar('/pdis'));
        }}>Excluir</button>}
        <Link className="btn" to="/pdis">Voltar</Link>
      </>}>
      <Alerta erro={acao.erro} />
      <div className="grade grade-2">
        <Cartao titulo="Colaborador">
          <div className="grade grade-2 pequeno">
            <div><span className="campo-rotulo">Departamento</span><div>{p.departamento}</div></div>
            <div><span className="campo-rotulo">Cargo atual</span><div>{p.cargoAtual}</div></div>
            <div><span className="campo-rotulo">Gestor</span><div>{p.gestorNome ?? '—'}</div></div>
            <div><span className="campo-rotulo">Data de admissão</span><div>{fmtData(p.dataAdmissao)}</div></div>
            <div><span className="campo-rotulo">Cargo desejado</span><div>{p.cargoDesejado ?? '—'}</div></div>
            <div><span className="campo-rotulo">Ciclo</span><div>{p.cicloNome ?? '—'}</div></div>
          </div>
        </Cartao>
        <Cartao titulo="Progresso geral">
          <Progresso valor={p.progressoGeral} />
          <p className="texto-2 pequeno" style={{ marginTop: 8 }}>
            {p.acoes.filter((a) => a.status === 'CONCLUIDO').length} de {p.acoes.length} ações concluídas · criado em {fmtData(p.criadoEm)}
          </p>
        </Cartao>
      </div>
      <div className="grade grade-3">
        <Cartao titulo="Habilidades a desenvolver"><Lista texto={p.habilidadesDesenvolver} /></Cartao>
        <Cartao titulo="Habilidades de sucesso"><Lista texto={p.habilidadesSucesso} /></Cartao>
        <Cartao titulo="Anotações"><p className="pre">{p.anotacoes || '—'}</p></Cartao>
      </div>

      <Cartao titulo="Plano de ação" acoes={podeEditar && ativo && (
        <button className="btn-primario" onClick={() => { acao.setErro(null); setEditAcao(ACAO_VAZIA); }}>+ Nova ação</button>
      )}>
        {p.acoes.length === 0 ? <Vazio>Nenhuma ação cadastrada.</Vazio> : (
          <div className="tabela-wrap">
            <table>
              <thead><tr><th>Ação</th><th>Período</th><th>Entrega</th><th>Recursos</th><th>Status</th><th>Progresso</th><th /></tr></thead>
              <tbody>
                {p.acoes.map((a) => (
                  <tr key={a.id}>
                    <td><strong>{a.descricao}</strong></td>
                    <td>{fmtData(a.dataInicio)} a {fmtData(a.dataTermino)}{a.atrasada && <div><Badge status="ERRO">Atrasada</Badge></div>}</td>
                    <td>{a.entrega || '—'}</td>
                    <td>{a.recursos || '—'}</td>
                    <td><Badge status={a.status} grupo="statusAcao" /></td>
                    <td style={{ minWidth: 120 }}><Progresso valor={a.progresso} /></td>
                    <td className="acoes">
                      {ativo && (ehDono || podeEditar) && (
                        <button className="btn-texto" onClick={() => { acao.setErro(null); setProgresso({ id: a.id, status: a.status, progresso: a.progresso, descricao: a.descricao }); }}>
                          Atualizar progresso
                        </button>
                      )}
                      {podeEditar && <button className="btn-texto" onClick={() => { acao.setErro(null); setEditAcao({ ...a }); }}>Editar</button>}
                      {podeEditar && <button className="btn-texto btn-perigo" onClick={() => {
                        if (window.confirm('Excluir esta ação?')) exec(() => api.del(`/acoes-pdi/${a.id}`));
                      }}>Excluir</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Cartao>

      <Modal titulo="Editar PDI" largo aberto={editPdi !== null} onFechar={() => setEditPdi(null)}>
        {editPdi && (
          <form className="form" onSubmit={salvarPdi}>
            <Alerta erro={acao.erro} />
            <FormPDI form={editPdi} setForm={setEditPdi} travarColaborador
              pessoas={[{ id: p.colaboradorId, nome: p.colaboradorNome, cargoNome: p.cargoAtual }]} ciclos={ciclos.data || []} />
            <Campo rotulo="Situação do PDI">
              <Seletor valor={editPdi.status} onChange={(v) => setEditPdi({ ...editPdi, status: v })}
                opcoes={Object.entries(ROTULOS.statusPDI).map(([valor, rotulo]) => ({ valor, rotulo }))} />
            </Campo>
            <div className="form-acoes">
              <button type="button" onClick={() => setEditPdi(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        )}
      </Modal>

      <Modal titulo={editAcao?.id ? 'Editar ação' : 'Nova ação'} aberto={editAcao !== null} onFechar={() => setEditAcao(null)}>
        {editAcao && (
          <form className="form" onSubmit={salvarAcao}>
            <Alerta erro={acao.erro} />
            <Campo rotulo="Ação"><textarea value={editAcao.descricao} rows={2} required onChange={(e) => setEditAcao({ ...editAcao, descricao: e.target.value })} /></Campo>
            <div className="grade grade-2">
              <Campo rotulo="Início"><input type="date" value={editAcao.dataInicio} required onChange={(e) => setEditAcao({ ...editAcao, dataInicio: e.target.value })} /></Campo>
              <Campo rotulo="Término" dica="Não pode ser anterior ao início">
                <input type="date" value={editAcao.dataTermino} min={editAcao.dataInicio} required onChange={(e) => setEditAcao({ ...editAcao, dataTermino: e.target.value })} />
              </Campo>
            </div>
            <Campo rotulo="Entrega esperada"><input value={editAcao.entrega || ''} onChange={(e) => setEditAcao({ ...editAcao, entrega: e.target.value })} /></Campo>
            <Campo rotulo="Recursos necessários" dica='Texto livre; pode incluir valor, ex.: "Curso online - R$ 51,35"'>
              <input value={editAcao.recursos || ''} onChange={(e) => setEditAcao({ ...editAcao, recursos: e.target.value })} />
            </Campo>
            <div className="grade grade-2">
              <Campo rotulo="Status"><Seletor valor={editAcao.status} onChange={(v) => setEditAcao({ ...editAcao, status: v })} opcoes={opcoesStatus} /></Campo>
              <Campo rotulo={`Progresso: ${editAcao.progresso}%`}>
                <input type="range" min={0} max={100} step={5} value={editAcao.progresso} onChange={(e) => setEditAcao({ ...editAcao, progresso: e.target.value })} />
              </Campo>
            </div>
            <div className="form-acoes">
              <button type="button" onClick={() => setEditAcao(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        )}
      </Modal>

      <Modal titulo="Atualizar progresso" aberto={progresso !== null} onFechar={() => setProgresso(null)}>
        {progresso && (
          <form className="form" onSubmit={salvarProgresso}>
            <Alerta erro={acao.erro} />
            <p><strong>{progresso.descricao}</strong></p>
            <Campo rotulo="Status"><Seletor valor={progresso.status} onChange={(v) => setProgresso({ ...progresso, status: v, progresso: v === 'CONCLUIDO' ? 100 : progresso.progresso })} opcoes={opcoesStatus} /></Campo>
            <Campo rotulo={`Progresso: ${progresso.progresso}%`}>
              <input type="range" min={0} max={100} step={5} value={progresso.progresso} onChange={(e) => setProgresso({ ...progresso, progresso: e.target.value })} />
            </Campo>
            <div className="form-acoes">
              <button type="button" onClick={() => setProgresso(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        )}
      </Modal>
    </Pagina>
  );
}
