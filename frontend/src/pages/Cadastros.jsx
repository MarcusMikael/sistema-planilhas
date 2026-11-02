import { useState } from 'react';
import { api } from '../api.js';
import { useAcao, useApi } from '../hooks.js';
import { Alerta, Cartao, Carregando, Pagina } from '../components/ui.jsx';

function ListaEditavel({ titulo, recurso }) {
  const lista = useApi(`/${recurso}`);
  const [novo, setNovo] = useState('');
  const [editando, setEditando] = useState(null);
  const acao = useAcao();

  async function executar(fn) {
    try {
      await acao.executar(fn);
      lista.recarregar();
      return true;
    } catch {
      return false;
    }
  }

  return (
    <Cartao titulo={titulo}>
      <form className="linha" style={{ marginBottom: 12 }} onSubmit={async (e) => {
        e.preventDefault();
        if (await executar(() => api.post(`/${recurso}`, { nome: novo }))) setNovo('');
      }}>
        <input value={novo} onChange={(e) => setNovo(e.target.value)} placeholder="Nome" required style={{ flex: 1 }} />
        <button className="btn-primario" disabled={acao.enviando}>Adicionar</button>
      </form>
      <Alerta erro={acao.erro} />
      {lista.carregando ? <Carregando /> : (
        <table>
          <tbody>
            {(lista.data || []).map((item) => (
              <tr key={item.id}>
                <td>
                  {editando?.id === item.id ? (
                    <input value={editando.nome} autoFocus onChange={(e) => setEditando({ ...editando, nome: e.target.value })} />
                  ) : item.nome}
                </td>
                <td className="acoes">
                  {editando?.id === item.id ? (
                    <>
                      <button className="btn-texto" onClick={async () => {
                        if (await executar(() => api.put(`/${recurso}/${item.id}`, { nome: editando.nome, descricao: item.descricao }))) setEditando(null);
                      }}>Salvar</button>
                      <button className="btn-texto" onClick={() => setEditando(null)}>Cancelar</button>
                    </>
                  ) : (
                    <>
                      <button className="btn-texto" onClick={() => setEditando({ id: item.id, nome: item.nome })}>Editar</button>
                      <button className="btn-texto btn-perigo" onClick={() => {
                        if (window.confirm(`Excluir "${item.nome}"?`)) executar(() => api.del(`/${recurso}/${item.id}`));
                      }}>Excluir</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Cartao>
  );
}

export default function Cadastros() {
  return (
    <Pagina titulo="Departamentos e cargos" subtitulo="Tabelas de apoio do cadastro de colaboradores">
      <div className="grade grade-2">
        <ListaEditavel titulo="Departamentos" recurso="departamentos" />
        <ListaEditavel titulo="Cargos" recurso="cargos" />
      </div>
    </Pagina>
  );
}
