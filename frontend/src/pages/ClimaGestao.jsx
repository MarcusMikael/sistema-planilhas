import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Seletor } from '../components/ui.jsx';
import { ROTULOS, fmtData } from '../format.js';

const opcoesTipo = Object.entries(ROTULOS.tipoPergunta).map(([valor, rotulo]) => ({ valor, rotulo }));

export default function ClimaGestao() {
  const { id } = useParams();
  const pesquisa = useApi(`/pesquisas-clima/${id}`);
  const [pergunta, setPergunta] = useState(null);
  const [dados, setDados] = useState(null);
  const acao = useAcao();
  const navegar = useNavigate();

  if (pesquisa.carregando) return <Carregando />;
  if (pesquisa.erro) return <Alerta erro={pesquisa.erro} />;
  const p = pesquisa.data;
  const rascunho = p.status === 'RASCUNHO';

  async function exec(fn, depois) {
    try {
      await acao.executar(fn);
      depois?.();
      pesquisa.recarregar();
    } catch {
      /* exibido */
    }
  }

  const salvarPergunta = (e) => {
    e.preventDefault();
    const corpo = { texto: pergunta.texto, tipo: pergunta.tipo, obrigatoria: pergunta.obrigatoria, ordem: pergunta.ordem ? Number(pergunta.ordem) : null };
    exec(() => (pergunta.id ? api.put(`/pesquisas-clima/perguntas/${pergunta.id}`, corpo)
      : api.post(`/pesquisas-clima/${id}/perguntas`, corpo)), () => setPergunta(null));
  };

  return (
    <Pagina titulo={p.titulo} subtitulo={`${fmtData(p.dataInicio)} a ${fmtData(p.dataFim)} · ${p.totalParticipacoes} participação(ões)`}
      acoes={<>
        <Badge status={p.status} grupo="statusPesquisa" />
        {p.status !== 'ENCERRADA' && <button onClick={() => { acao.setErro(null); setDados({ titulo: p.titulo, dataInicio: p.dataInicio, dataFim: p.dataFim }); }}>Editar dados</button>}
        {rascunho && <button className="btn-primario" onClick={() => {
          if (window.confirm('Publicar a campanha? Depois disso as perguntas não poderão ser alteradas.')) exec(() => api.post(`/pesquisas-clima/${id}/publicar`));
        }}>Publicar</button>}
        {p.status === 'PUBLICADA' && <button onClick={() => {
          if (window.confirm('Encerrar a campanha? Ninguém mais poderá responder.')) exec(() => api.post(`/pesquisas-clima/${id}/encerrar`));
        }}>Encerrar</button>}
        {!rascunho && <Link className="btn" to={`/clima/${id}/resultados`}>Resultados</Link>}
        {rascunho && <button className="btn-perigo" onClick={() => {
          if (window.confirm('Excluir esta campanha?')) exec(() => api.del(`/pesquisas-clima/${id}`), () => navegar('/clima'));
        }}>Excluir</button>}
      </>}>
      <Alerta erro={acao.erro} />
      {rascunho && <Alerta tipo="info">Personalize as perguntas antes de publicar. A pergunta de setor é preenchida automaticamente pelo cadastro.</Alerta>}
      <Cartao titulo={`Perguntas (${p.perguntas.length})`}
        acoes={rascunho && <button className="btn-primario" onClick={() => { acao.setErro(null); setPergunta({ texto: '', tipo: 'ESCALA', obrigatoria: true, ordem: '' }); }}>+ Pergunta</button>}>
        <table>
          <thead><tr><th>Nº</th><th>Pergunta</th><th>Tipo</th><th>Obrigatória</th><th /></tr></thead>
          <tbody>
            {p.perguntas.map((q) => (
              <tr key={q.id}>
                <td>{q.ordem}</td>
                <td>{q.texto}</td>
                <td>{ROTULOS.tipoPergunta[q.tipo]}</td>
                <td>{q.obrigatoria ? 'Sim' : 'Não'}</td>
                <td className="acoes">{rascunho && <>
                  <button className="btn-texto" onClick={() => { acao.setErro(null); setPergunta({ ...q }); }}>Editar</button>
                  <button className="btn-texto btn-perigo" onClick={() => {
                    if (window.confirm('Remover esta pergunta?')) exec(() => api.del(`/pesquisas-clima/perguntas/${q.id}`));
                  }}>Remover</button>
                </>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Cartao>

      <Modal titulo={pergunta?.id ? 'Editar pergunta' : 'Nova pergunta'} aberto={pergunta !== null} onFechar={() => setPergunta(null)}>
        {pergunta && (
          <form className="form" onSubmit={salvarPergunta}>
            <Alerta erro={acao.erro} />
            <Campo rotulo="Texto"><textarea value={pergunta.texto} required onChange={(e) => setPergunta({ ...pergunta, texto: e.target.value })} /></Campo>
            <div className="grade grade-2">
              <Campo rotulo="Tipo"><Seletor valor={pergunta.tipo} onChange={(v) => setPergunta({ ...pergunta, tipo: v })} opcoes={opcoesTipo} /></Campo>
              <Campo rotulo="Ordem" dica="Em branco: ao final"><input type="number" min={1} value={pergunta.ordem ?? ''} onChange={(e) => setPergunta({ ...pergunta, ordem: e.target.value })} /></Campo>
            </div>
            <label className="check"><input type="checkbox" checked={pergunta.obrigatoria} onChange={(e) => setPergunta({ ...pergunta, obrigatoria: e.target.checked })} /> Obrigatória</label>
            <div className="form-acoes">
              <button type="button" onClick={() => setPergunta(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        )}
      </Modal>

      <Modal titulo="Dados da campanha" aberto={dados !== null} onFechar={() => setDados(null)}>
        {dados && (
          <form className="form" onSubmit={(e) => { e.preventDefault(); exec(() => api.put(`/pesquisas-clima/${id}`, dados), () => setDados(null)); }}>
            <Alerta erro={acao.erro} />
            <Campo rotulo="Título"><input value={dados.titulo} required onChange={(e) => setDados({ ...dados, titulo: e.target.value })} /></Campo>
            <div className="grade grade-2">
              <Campo rotulo="Início"><input type="date" value={dados.dataInicio} required onChange={(e) => setDados({ ...dados, dataInicio: e.target.value })} /></Campo>
              <Campo rotulo="Término"><input type="date" value={dados.dataFim} required onChange={(e) => setDados({ ...dados, dataFim: e.target.value })} /></Campo>
            </div>
            <div className="form-acoes">
              <button type="button" onClick={() => setDados(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
            </div>
          </form>
        )}
      </Modal>
    </Pagina>
  );
}
