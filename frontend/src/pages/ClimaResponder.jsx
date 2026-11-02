import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Cartao, Carregando, Pagina } from '../components/ui.jsx';
import { ESCALA_CLIMA, fmtData } from '../format.js';

export default function ClimaResponder() {
  const { id } = useParams();
  const pesquisa = useApi(`/pesquisas-clima/${id}`);
  const [respostas, setRespostas] = useState({});
  const [enviado, setEnviado] = useState(false);
  const acao = useAcao();

  if (pesquisa.carregando) return <Carregando />;
  if (pesquisa.erro) return <Pagina titulo="Pesquisa de clima"><Alerta erro={pesquisa.erro} /></Pagina>;
  const p = pesquisa.data;

  if (enviado || p.jaRespondeu) {
    return (
      <Pagina titulo={p.titulo}>
        <Alerta tipo="ok">Obrigado! Sua participação foi registrada. As respostas não ficam associadas a você.</Alerta>
        <div><Link className="btn" to="/clima">Voltar</Link></div>
      </Pagina>
    );
  }
  if (!p.abertaParaResposta) {
    return <Pagina titulo={p.titulo}><Alerta tipo="aviso">Esta pesquisa não está aberta para respostas.</Alerta></Pagina>;
  }

  const perguntas = p.perguntas.filter((q) => q.tipo !== 'SETOR');
  const faltam = perguntas.filter((q) => q.obrigatoria && (respostas[q.id] === undefined || respostas[q.id] === '')).length;

  async function enviar(e) {
    e.preventDefault();
    if (!window.confirm('Enviar as respostas? Não será possível alterá-las depois.')) return;
    const corpo = {
      respostas: perguntas.map((q) => (q.tipo === 'ESCALA'
        ? { perguntaId: q.id, valor: respostas[q.id] ?? null }
        : { perguntaId: q.id, texto: respostas[q.id] ?? null })),
    };
    try {
      await acao.executar(() => api.post(`/pesquisas-clima/${id}/respostas`, corpo));
      setEnviado(true);
    } catch {
      /* exibido */
    }
  }

  return (
    <Pagina titulo={p.titulo} subtitulo={`Aberta até ${fmtData(p.dataFim)}`}>
      <Alerta tipo="info">
        Suas respostas são anônimas. O setor é preenchido a partir do seu cadastro e os resultados só são exibidos
        de forma agregada, para grupos com um número mínimo de respostas.
      </Alerta>
      <form className="form" onSubmit={enviar}>
        {perguntas.map((q, idx) => (
          <Cartao key={q.id}>
            <div className="form">
              <strong>{idx + 1}. {q.texto}{q.obrigatoria && ' *'}</strong>
              {q.tipo === 'ESCALA' ? (
                <div className="escala-clima" role="radiogroup">
                  {ESCALA_CLIMA.map((op) => (
                    <label key={op.valor} className={respostas[q.id] === op.valor ? 'selecionada' : ''}>
                      <input type="radio" name={`q${q.id}`} checked={respostas[q.id] === op.valor}
                        onChange={() => setRespostas({ ...respostas, [q.id]: op.valor })} />
                      {op.valor} – {op.rotulo}
                    </label>
                  ))}
                </div>
              ) : (
                <textarea value={respostas[q.id] || ''} onChange={(e) => setRespostas({ ...respostas, [q.id]: e.target.value })} />
              )}
            </div>
          </Cartao>
        ))}
        <Alerta erro={acao.erro} />
        <div className="form-acoes">
          <span className="texto-2">{faltam > 0 ? `${faltam} pergunta(s) obrigatória(s) sem resposta` : 'Tudo pronto'}</span>
          <button className="btn-primario" disabled={acao.enviando || faltam > 0}>Enviar respostas</button>
        </div>
      </form>
    </Pagina>
  );
}
