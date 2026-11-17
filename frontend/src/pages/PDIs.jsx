import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Cartao, Carregando, Modal, Pagina, Progresso, Vazio } from '../components/ui.jsx';
import { fmtData } from '../format.js';
import { FormPDI } from '../components/FormPDI.jsx';
import { PDI_VAZIO } from '../pdi.js';

export default function PDIs() {
  const { isRH, isGestor, usuario } = useAuth();
  const [params] = useSearchParams();
  const filtro = params.get('colaborador');
  const lista = useApi(filtro ? `/pdis?colaboradorId=${filtro}` : '/pdis');
  const pessoas = useApi(isRH || isGestor ? (isRH ? '/colaboradores' : '/colaboradores/equipe') : null);
  const ciclos = useApi(isRH || isGestor ? '/ciclos' : null);
  const [form, setForm] = useState(null);
  const acao = useAcao();
  const navegar = useNavigate();
  const podeCriar = isRH || (isGestor && (pessoas.data || []).length > 0);

  async function criar(e) {
    e.preventDefault();
    try {
      const p = await acao.executar(() => api.post('/pdis', form));
      navegar(`/pdis/${p.id}`);
    } catch {
      /* exibido */
    }
  }

  return (
    <Pagina titulo={isRH || isGestor ? 'Planos de Desenvolvimento Individual' : 'Meu PDI'}
      subtitulo="Objetivos, habilidades e ações com prazo, entrega, recursos e status"
      acoes={podeCriar && (
        <button className="btn-primario" onClick={() => { acao.setErro(null); setForm({ ...PDI_VAZIO, colaboradorId: filtro ? Number(filtro) : null }); }}>
          + Novo PDI
        </button>
      )}>
      <Alerta erro={lista.erro} />
      {lista.carregando ? <Carregando /> : (lista.data || []).length === 0 ? (
        <Vazio>{isRH || isGestor ? 'Nenhum PDI cadastrado.' : 'Você ainda não possui PDI. Ele é criado pelo seu gestor ou pelo RH.'}</Vazio>
      ) : (
        <Cartao>
          <div className="tabela-wrap">
            <table>
              <thead><tr>{(isRH || isGestor) && <th>Colaborador</th>}<th>Objetivo</th><th>Criado em</th><th>Ações</th><th>Progresso</th><th>Situação</th><th /></tr></thead>
              <tbody>
                {lista.data.map((p) => (
                  <tr key={p.id}>
                    {(isRH || isGestor) && <td><strong>{p.colaboradorNome}</strong>{p.colaboradorId === usuario.colaboradorId && ' (você)'}<div className="texto-2 pequeno">{p.departamento}</div></td>}
                    <td>{p.objetivo}</td>
                    <td>{fmtData(p.criadoEm)}</td>
                    <td>{p.acoes.length}{p.acoes.some((a) => a.atrasada) && <> · <Badge status="ERRO">atrasada</Badge></>}</td>
                    <td style={{ minWidth: 130 }}><Progresso valor={p.progressoGeral} /></td>
                    <td><Badge status={p.status} grupo="statusPDI" /></td>
                    <td className="acoes"><Link to={`/pdis/${p.id}`}>Abrir →</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Cartao>
      )}
      <Modal titulo="Novo PDI" largo aberto={form !== null} onFechar={() => setForm(null)}>
        {form && (
          <form className="form" onSubmit={criar}>
            <Alerta erro={acao.erro} />
            <FormPDI form={form} setForm={setForm} pessoas={(pessoas.data || []).filter((p) => p.ativo)} ciclos={ciclos.data || []} />
            <div className="form-acoes">
              <button type="button" onClick={() => setForm(null)}>Cancelar</button>
              <button className="btn-primario" disabled={acao.enviando}>Criar PDI</button>
            </div>
          </form>
        )}
      </Modal>
    </Pagina>
  );
}
