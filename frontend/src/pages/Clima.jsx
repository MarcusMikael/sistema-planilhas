import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Vazio } from '../components/ui.jsx';
import { fmtData } from '../format.js';

export default function Clima() {
  const { isRH, usuario } = useAuth();
  const lista = useApi('/pesquisas-clima');
  const [nova, setNova] = useState(null);
  const acao = useAcao();
  const navegar = useNavigate();

  async function criar(e) {
    e.preventDefault();
    try {
      const p = await acao.executar(() => api.post('/pesquisas-clima', nova));
      navegar(`/clima/${p.id}/gestao`);
    } catch {
      /* exibido */
    }
  }

  return (
    <Pagina titulo="Pesquisa de clima"
      subtitulo="As respostas são anônimas: o sistema registra apenas que você participou, sem ligar o conteúdo a você."
      acoes={isRH && <button className="btn-primario" onClick={() => { acao.setErro(null); setNova({ titulo: '', dataInicio: '', dataFim: '', usarQuestionarioPadrao: true }); }}>+ Nova campanha</button>}>
      <Alerta erro={lista.erro} />
      {lista.carregando ? <Carregando /> : (lista.data || []).length === 0 ? <Vazio>Nenhuma pesquisa disponível.</Vazio> : (
        <Cartao>
          <table>
            <thead><tr><th>Campanha</th><th>Período</th><th>Perguntas</th>{isRH && <th>Participações</th>}<th>Situação</th><th /></tr></thead>
            <tbody>
              {lista.data.map((p) => (
                <tr key={p.id}>
                  <td><strong>{p.titulo}</strong></td>
                  <td>{fmtData(p.dataInicio)} a {fmtData(p.dataFim)}</td>
                  <td>{p.totalPerguntas}</td>
                  {isRH && <td>{p.totalParticipacoes}</td>}
                  <td>
                    <Badge status={p.status} grupo="statusPesquisa" />
                    {p.jaRespondeu && <> <Badge status="CONCLUIDA">Respondida</Badge></>}
                  </td>
                  <td className="acoes">
                    {usuario.colaboradorId && p.abertaParaResposta && !p.jaRespondeu && (
                      <Link className="btn btn-primario" to={`/clima/${p.id}/responder`}>Responder</Link>
                    )}
                    {isRH && <Link className="btn-texto" to={`/clima/${p.id}/gestao`}>{p.status === 'RASCUNHO' ? 'Editar' : 'Gerenciar'}</Link>}
                    {isRH && p.status !== 'RASCUNHO' && <Link className="btn-texto" to={`/clima/${p.id}/resultados`}>Resultados</Link>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Cartao>
      )}
      <Modal titulo="Nova campanha de clima" aberto={nova !== null} onFechar={() => setNova(null)}>
        {nova && (
          <form className="form" onSubmit={criar}>
            <Alerta erro={acao.erro} />
            <Campo rotulo="Título"><input value={nova.titulo} onChange={(e) => setNova({ ...nova, titulo: e.target.value })} required /></Campo>
            <div className="grade grade-2">
              <Campo rotulo="Início"><input type="date" value={nova.dataInicio} onChange={(e) => setNova({ ...nova, dataInicio: e.target.value })} required /></Campo>
              <Campo rotulo="Término"><input type="date" value={nova.dataFim} min={nova.dataInicio} onChange={(e) => setNova({ ...nova, dataFim: e.target.value })} required /></Campo>
            </div>
            <label className="check"><input type="checkbox" checked={nova.usarQuestionarioPadrao}
              onChange={(e) => setNova({ ...nova, usarQuestionarioPadrao: e.target.checked })} /> Usar o questionário de referência (20 perguntas)</label>
            <div className="form-acoes">
              <button type="button" onClick={() => setNova(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Criar</button>
            </div>
          </form>
        )}
      </Modal>
    </Pagina>
  );
}
