import { useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Cartao, Carregando, Pagina, Vazio } from '../components/ui.jsx';
import { ROTULOS, fmtData } from '../format.js';

export default function Avaliacoes() {
  const { usuario, isGestor } = useAuth();
  const pend = useApi('/avaliacoes/pendentes');
  const acao = useAcao();
  const navegar = useNavigate();

  async function abrir(p) {
    if (p.avaliacaoId) {
      navegar(`/avaliacoes/${p.avaliacaoId}`);
      return;
    }
    try {
      const a = await acao.executar(() => api.post('/avaliacoes', { cicloId: p.cicloId, colaboradorId: p.colaboradorId, tipo: p.tipo }));
      navegar(`/avaliacoes/${a.id}`);
    } catch {
      /* exibido */
    }
  }

  if (!usuario.colaboradorId) {
    return <Pagina titulo="Avaliações"><Vazio>Sua conta não está vinculada a um colaborador.</Vazio></Pagina>;
  }
  const lista = pend.data || [];
  const auto = lista.filter((p) => p.tipo === 'AUTOAVALIACAO');
  const equipe = lista.filter((p) => p.tipo === 'GESTOR');

  const tabela = (itens, mostrarPessoa) => (
    <table>
      <thead><tr>{mostrarPessoa && <th>Colaborador</th>}<th>Ciclo</th><th>Prazo</th><th>Situação</th><th /></tr></thead>
      <tbody>
        {itens.map((p) => (
          <tr key={`${p.cicloId}-${p.colaboradorId}-${p.tipo}`}>
            {mostrarPessoa && <td><strong>{p.colaboradorNome}</strong><div className="texto-2 pequeno">{p.colaboradorCargo}</div></td>}
            <td>{p.cicloNome}</td>
            <td>{fmtData(p.cicloDataFim)}</td>
            <td>{p.status ? <Badge status={p.status} grupo="statusAvaliacao" /> : <Badge status="PENDENTE">Não iniciada</Badge>}</td>
            <td className="acoes">
              <button className={p.status === 'CONCLUIDA' ? '' : 'btn-primario'} onClick={() => abrir(p)} disabled={acao.enviando}>
                {p.status === 'CONCLUIDA' ? 'Ver' : p.status ? 'Continuar' : 'Iniciar'}
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  return (
    <Pagina titulo="Avaliações de desempenho"
      subtitulo="Notas de 0 a 5 por competência. A autoavaliação e a avaliação do gestor são registradas separadamente.">
      <Alerta erro={pend.erro || acao.erro} />
      {pend.carregando ? <Carregando /> : (
        <>
          <Cartao titulo={ROTULOS.tipoAvaliacao.AUTOAVALIACAO}>
            {auto.length === 0 ? <Vazio>Não há ciclo aberto para avaliações no momento.</Vazio> : tabela(auto, false)}
          </Cartao>
          {(isGestor || equipe.length > 0) && (
            <Cartao titulo="Avaliar minha equipe">
              {equipe.length === 0 ? <Vazio>Nenhuma avaliação da equipe pendente.</Vazio> : tabela(equipe, true)}
            </Cartao>
          )}
        </>
      )}
    </Pagina>
  );
}
