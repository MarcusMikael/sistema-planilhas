import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Vazio } from '../components/ui.jsx';
import { fmtData } from '../format.js';

export default function Ciclos() {
  const lista = useApi('/ciclos');
  const [novo, setNovo] = useState(null);
  const acao = useAcao();
  const navegar = useNavigate();

  async function criar(e) {
    e.preventDefault();
    try {
      const c = await acao.executar(() => api.post('/ciclos', novo));
      navegar(`/ciclos/${c.id}`);
    } catch {
      /* exibido */
    }
  }

  return (
    <Pagina titulo="Ciclos de avaliação"
      subtitulo="Crie o ciclo, selecione 5 competências comportamentais e 5 técnicas e libere para as avaliações"
      acoes={<button className="btn-primario" onClick={() => { acao.setErro(null); setNovo({ nome: '', dataInicio: '', dataFim: '' }); }}>+ Novo ciclo</button>}>
      <Cartao>
        {lista.carregando ? <Carregando /> : (lista.data || []).length === 0 ? <Vazio>Nenhum ciclo criado.</Vazio> : (
          <table>
            <thead><tr><th>Ciclo</th><th>Período</th><th>Competências</th><th>Situação</th><th /></tr></thead>
            <tbody>
              {lista.data.map((c) => (
                <tr key={c.id}>
                  <td><strong>{c.nome}</strong></td>
                  <td>{fmtData(c.dataInicio)} a {fmtData(c.dataFim)}</td>
                  <td>{c.qtdComportamentais} comportamentais · {c.qtdTecnicas} técnicas</td>
                  <td><Badge status={c.status} grupo="statusCiclo" /></td>
                  <td className="acoes"><Link to={`/ciclos/${c.id}`}>Gerenciar →</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Cartao>
      <Modal titulo="Novo ciclo" aberto={novo !== null} onFechar={() => setNovo(null)}>
        {novo && (
          <form className="form" onSubmit={criar}>
            <Alerta erro={acao.erro} />
            <Campo rotulo="Nome"><input value={novo.nome} onChange={(e) => setNovo({ ...novo, nome: e.target.value })} required placeholder="Ex.: Ciclo 2026/2" /></Campo>
            <div className="grade grade-2">
              <Campo rotulo="Início"><input type="date" value={novo.dataInicio} onChange={(e) => setNovo({ ...novo, dataInicio: e.target.value })} required /></Campo>
              <Campo rotulo="Término"><input type="date" value={novo.dataFim} min={novo.dataInicio} onChange={(e) => setNovo({ ...novo, dataFim: e.target.value })} required /></Campo>
            </div>
            <div className="form-acoes">
              <button type="button" onClick={() => setNovo(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Criar e selecionar competências</button>
            </div>
          </form>
        )}
      </Modal>
    </Pagina>
  );
}
