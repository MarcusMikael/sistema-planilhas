import { Campo, Seletor } from './ui.jsx';

/** Campos do formulário de PDI, usados na criação (PDIs) e na edição (PDIDetalhe). */
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
