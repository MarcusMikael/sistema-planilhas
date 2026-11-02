import { useState } from 'react';
import { api } from '../api.js';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Badge, Campo, Cartao, Carregando, Modal, Pagina, Seletor } from '../components/ui.jsx';
import { ROTULOS } from '../format.js';

const VAZIO = { nome: '', descricao: '', tipo: 'COMPORTAMENTAL', ativo: true };

export default function Competencias() {
  const lista = useApi('/competencias');
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(VAZIO);
  const acao = useAcao();

  function abrir(c) {
    acao.setErro(null);
    setEditando(c ?? {});
    setForm(c ? { nome: c.nome, descricao: c.descricao || '', tipo: c.tipo, ativo: c.ativo } : VAZIO);
  }

  async function salvar(e) {
    e.preventDefault();
    try {
      await acao.executar(() => (editando.id ? api.put(`/competencias/${editando.id}`, form) : api.post('/competencias', form)));
      setEditando(null);
      lista.recarregar();
    } catch {
      /* exibido */
    }
  }

  const set = (campo) => (v) => setForm((f) => ({ ...f, [campo]: v }));

  return (
    <Pagina titulo="Catálogo de competências"
      subtitulo="Competências disponíveis para os ciclos. Cada ciclo usa 5 comportamentais e 5 técnicas."
      acoes={<button className="btn-primario" onClick={() => abrir(null)}>+ Nova competência</button>}>
      {lista.carregando ? <Carregando /> : (
        <div className="grade grade-2">
          {['COMPORTAMENTAL', 'TECNICA'].map((tipo) => (
            <Cartao key={tipo} titulo={ROTULOS.tipoCompetenciaPlural[tipo]}>
              <table>
                <tbody>
                  {(lista.data || []).filter((c) => c.tipo === tipo).map((c) => (
                    <tr key={c.id}>
                      <td>
                        <strong>{c.nome}</strong> {!c.ativo && <Badge status="CANCELADO">Inativa</Badge>}
                        <div className="texto-2 pequeno">{c.descricao}</div>
                      </td>
                      <td className="acoes"><button className="btn-texto" onClick={() => abrir(c)}>Editar</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Cartao>
          ))}
        </div>
      )}
      <Modal titulo={editando?.id ? 'Editar competência' : 'Nova competência'} aberto={editando !== null} onFechar={() => setEditando(null)}>
        <form className="form" onSubmit={salvar}>
          <Alerta erro={acao.erro} />
          <Campo rotulo="Nome"><input value={form.nome} onChange={(e) => set('nome')(e.target.value)} required /></Campo>
          <Campo rotulo="Descrição" dica="Exibida ao avaliador junto com a competência">
            <textarea value={form.descricao} onChange={(e) => set('descricao')(e.target.value)} />
          </Campo>
          <Campo rotulo="Tipo">
            <Seletor valor={form.tipo} onChange={set('tipo')}
              opcoes={Object.entries(ROTULOS.tipoCompetencia).map(([valor, rotulo]) => ({ valor, rotulo }))} />
          </Campo>
          <label className="check"><input type="checkbox" checked={form.ativo} onChange={(e) => set('ativo')(e.target.checked)} /> Ativa (pode ser selecionada em novos ciclos)</label>
          <div className="form-acoes">
            {editando?.id && (
              <button type="button" className="btn-perigo" onClick={async () => {
                if (!window.confirm('Excluir esta competência?')) return;
                try {
                  await acao.executar(() => api.del(`/competencias/${editando.id}`));
                  setEditando(null);
                  lista.recarregar();
                } catch {
                  /* exibido */
                }
              }}>Excluir</button>
            )}
            <div className="espaco" />
            <button type="button" onClick={() => setEditando(null)}>Cancelar</button>
            <button className="btn-primario" disabled={acao.enviando}>Salvar</button>
          </div>
        </form>
      </Modal>
    </Pagina>
  );
}
