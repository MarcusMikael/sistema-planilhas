import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Progresso, Seletor, Vazio } from '../components/ui.jsx';
import { fmtData } from '../format.js';

export const PDI_VAZIO = { colaboradorId: null, cicloId: null, cargoDesejado: '', objetivo: '', habilidadesDesenvolver: '',
  habilidadesSucesso: '', anotacoes: '' };

export function FormPDI({ form, setForm, pessoas, ciclos, travarColaborador }) {
  const set = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target ? e.target.value : e }));
  return (
    <>
      <div className="grade grade-2">
        <Campo rotulo="Colaborador">
          <Seletor valor={form.colaboradorId} onChange={(v) => setForm((f) => ({ ...f, colaboradorId: v && Number(v) }))}
            vazio="Selecione" required disabled={travarColaborador}
            opcoes={pessoas.map((p) => ({ valor: p.id, rotulo: `${p.nome} — ${p.cargoNome}` }))} />
        </Campo>
        <Campo rotulo="Ciclo relacionado (opcional)">
          <Seletor valor={form.cicloId} onChange={(v) => setForm((f) => ({ ...f, cicloId: v && Number(v) }))} vazio="Nenhum"
            opcoes={ciclos.map((c) => ({ valor: c.id, rotulo: c.nome }))} />
        </Campo>
      </div>
      <Campo rotulo="Objetivo principal"><textarea value={form.objetivo} onChange={set('objetivo')} required rows={2} /></Campo>
      <Campo rotulo="Cargo desejado (opcional)" dica="Preencha apenas se o PDI tiver esse objetivo">
        <input value={form.cargoDesejado || ''} onChange={set('cargoDesejado')} />
      </Campo>
      <div className="grade grade-2">
        <Campo rotulo="Habilidades a desenvolver" dica="Uma por linha">
          <textarea value={form.habilidadesDesenvolver || ''} onChange={set('habilidadesDesenvolver')} rows={4} />
        </Campo>
        <Campo rotulo="Habilidades de sucesso" dica="Pontos fortes que sustentam o desenvolvimento">
          <textarea value={form.habilidadesSucesso || ''} onChange={set('habilidadesSucesso')} rows={4} />
        </Campo>
      </div>
      <Campo rotulo="Anotações"><textarea value={form.anotacoes || ''} onChange={set('anotacoes')} rows={3} /></Campo>
    </>
  );
}

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
