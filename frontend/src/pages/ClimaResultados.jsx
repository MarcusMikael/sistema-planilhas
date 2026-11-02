import { Link, useParams } from 'react-router-dom';
import { useApi } from '../hooks.js';
import { Alerta, Badge, Cartao, Carregando, Indicador, Pagina } from '../components/ui.jsx';
import { ESCALA_CLIMA, fmtNum } from '../format.js';

export default function ClimaResultados() {
  const { id } = useParams();
  const res = useApi(`/pesquisas-clima/${id}/resultados`);
  if (res.carregando) return <Carregando />;
  if (res.erro) return <Alerta erro={res.erro} />;
  const r = res.data;
  const escala = r.perguntas.filter((q) => q.tipo === 'ESCALA');
  const exibidas = escala.filter((q) => q.exibido);
  const mediaGeral = exibidas.length ? exibidas.reduce((s, q) => s + q.media, 0) / exibidas.length : null;
  const setor = r.perguntas.find((q) => q.tipo === 'SETOR');

  return (
    <Pagina titulo={`Resultados — ${r.titulo}`}
      subtitulo={`Somente resultados agregados. Grupos com menos de ${r.minimoRespostas} respostas não são exibidos.`}
      acoes={<><Badge status={r.status} grupo="statusPesquisa" /><Link className="btn" to="/clima">Voltar</Link></>}>
      <div className="grade grade-3">
        <Indicador rotulo="Participações" valor={r.totalParticipacoes} />
        <Indicador rotulo="Média geral (escala 1 a 5)" valor={fmtNum(mediaGeral)} detalhe="Média das perguntas exibidas" />
        <Indicador rotulo="Mínimo para exibição" valor={r.minimoRespostas} detalhe="respostas por grupo" />
      </div>
      {setor && (
        <Cartao titulo="Participação por setor">
          <table>
            <tbody>
              {setor.porSetor.map((s) => (
                <tr key={s.setor}><td>{s.setor}</td><td className="num">{s.exibido ? s.totalRespostas : `menos de ${r.minimoRespostas}`}</td></tr>
              ))}
            </tbody>
          </table>
        </Cartao>
      )}
      {r.perguntas.filter((q) => q.tipo !== 'SETOR').map((q) => (
        <Cartao key={q.perguntaId} titulo={`${q.ordem}. ${q.texto}`}>
          {!q.exibido ? (
            <Alerta tipo="aviso">Resultado não exibido: {q.totalRespostas} resposta(s), abaixo do mínimo de {r.minimoRespostas}.</Alerta>
          ) : q.tipo === 'ESCALA' ? (
            <div className="grade grade-2">
              <div className="barras">
                <p><strong>Média {fmtNum(q.media)}</strong> <span className="texto-2">({q.totalRespostas} respostas)</span></p>
                {q.distribuicao.map((n, i) => (
                  <div className="barra-linha" key={i}>
                    <span>{ESCALA_CLIMA[i].valor} – {ESCALA_CLIMA[i].rotulo}</span>
                    <div className="barra-fundo"><div className="barra-cheia" style={{ width: `${(n / q.totalRespostas) * 100}%` }} /></div>
                    <span className="num">{n}</span>
                  </div>
                ))}
              </div>
              <table>
                <thead><tr><th>Setor</th><th className="num">Respostas</th><th className="num">Média</th></tr></thead>
                <tbody>
                  {q.porSetor.map((s) => (
                    <tr key={s.setor}>
                      <td>{s.setor}</td>
                      <td className="num">{s.exibido ? s.totalRespostas : `< ${r.minimoRespostas}`}</td>
                      <td className="num">{s.exibido ? fmtNum(s.media) : 'oculto'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <ul>{(q.respostasTexto || []).map((t, i) => <li key={i}>{t}</li>)}</ul>
          )}
        </Cartao>
      ))}
    </Pagina>
  );
}
