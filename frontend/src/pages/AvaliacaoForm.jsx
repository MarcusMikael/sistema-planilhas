import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Cartao, Carregando, Pagina } from '../components/ui.jsx';
import { ROTULOS, fmtData } from '../format.js';

const NOTAS = [0, 1, 2, 3, 4, 5];

export default function AvaliacaoForm() {
  const { id } = useParams();
  const { usuario } = useAuth();
  const av = useApi(`/avaliacoes/${id}`);
  const [itens, setItens] = useState([]);
  const [msg, setMsg] = useState(null);
  const acao = useAcao();

  useEffect(() => {
    if (av.data) setItens(av.data.itens.map((i) => ({ ...i })));
  }, [av.data]);

  if (av.carregando) return <Carregando />;
  if (av.erro) return <Pagina titulo="Avaliação"><Alerta erro={av.erro} /></Pagina>;
  const a = av.data;
  const editavel = a.status === 'EM_ANDAMENTO' && a.cicloStatus === 'LIBERADO' && a.avaliadorId === usuario.colaboradorId;
  const preenchidas = itens.filter((i) => i.nota !== null && i.nota !== undefined).length;

  const alterar = (ccId, campo, valor) =>
    setItens((xs) => xs.map((i) => (i.cicloCompetenciaId === ccId ? { ...i, [campo]: valor } : i)));

  const corpo = () => ({
    notas: itens.map((i) => ({ cicloCompetenciaId: i.cicloCompetenciaId, nota: i.nota, comentario: i.comentario || null })),
  });

  async function salvar() {
    try {
      av.setData(await acao.executar(() => api.put(`/avaliacoes/${id}/notas`, corpo())));
      setMsg('Rascunho salvo.');
    } catch {
      setMsg(null);
    }
  }

  async function concluir() {
    if (!window.confirm('Concluir a avaliação? Depois de concluída ela não poderá ser alterada.')) return;
    try {
      await acao.executar(() => api.put(`/avaliacoes/${id}/notas`, corpo()));
      av.setData(await acao.executar(() => api.post(`/avaliacoes/${id}/concluir`)));
      setMsg('Avaliação concluída.');
    } catch {
      setMsg(null);
    }
  }

  const grupo = (tipo) => itens.filter((i) => i.tipo === tipo);

  return (
    <Pagina
      titulo={ROTULOS.tipoAvaliacao[a.tipo]}
      subtitulo={a.cicloNome}
      acoes={<><Badge status={a.status} grupo="statusAvaliacao" /><Link className="btn" to="/avaliacoes">Voltar</Link></>}
    >
      <Cartao>
        <div className="grade grade-3">
          <div><span className="campo-rotulo">Avaliado(a)</span><div><strong>{a.colaboradorNome}</strong></div><div className="texto-2">{a.colaboradorCargo}</div></div>
          <div><span className="campo-rotulo">Avaliador(a)</span><div><strong>{a.avaliadorNome}</strong></div><div className="texto-2">{a.avaliadorCargo}</div></div>
          <div><span className="campo-rotulo">Data da avaliação</span><div>{a.dataAvaliacao ? fmtData(a.dataAvaliacao) : 'Registrada ao concluir'}</div>
            <div className="texto-2">{preenchidas} de {itens.length} notas preenchidas</div></div>
        </div>
      </Cartao>
      <Alerta erro={acao.erro} />
      {msg && <Alerta tipo="ok">{msg}</Alerta>}
      {editavel && (
        <Alerta tipo="info">
          Atribua uma nota de 0 a 5 a cada competência. Uma nota em branco não é considerada zero: todas precisam ser preenchidas para concluir.
        </Alerta>
      )}

      {['COMPORTAMENTAL', 'TECNICA'].map((tipo) => (
        <Cartao key={tipo} titulo={`Competências ${ROTULOS.tipoCompetenciaPlural[tipo].toLowerCase()}`}>
          <div className="form">
            {grupo(tipo).map((i) => (
              <div className="competencia-item" key={i.cicloCompetenciaId}>
                <div>
                  <h3>{i.nome}</h3>
                  <p className="texto-2 pequeno">{i.descricao}</p>
                </div>
                <div className="notas" role="radiogroup" aria-label={`Nota para ${i.nome}`}>
                  {NOTAS.map((n) => (
                    <button key={n} type="button" disabled={!editavel} aria-pressed={i.nota === n}
                      className={`nota-btn ${i.nota === n ? 'selecionada' : ''}`}
                      onClick={() => alterar(i.cicloCompetenciaId, 'nota', i.nota === n ? null : n)}>
                      {n}
                    </button>
                  ))}
                </div>
                {(editavel || i.comentario) && (
                  <div className="comentario">
                    <textarea placeholder="Comentário (opcional)" value={i.comentario || ''} disabled={!editavel} rows={2}
                      onChange={(e) => alterar(i.cicloCompetenciaId, 'comentario', e.target.value)} />
                  </div>
                )}
              </div>
            ))}
          </div>
        </Cartao>
      ))}

      {editavel && (
        <div className="form-acoes">
          <button onClick={salvar} disabled={acao.enviando}>Salvar rascunho</button>
          <button className="btn-primario" onClick={concluir} disabled={acao.enviando || preenchidas < itens.length}
            title={preenchidas < itens.length ? 'Preencha todas as notas para concluir' : ''}>Concluir avaliação</button>
        </div>
      )}
    </Pagina>
  );
}
