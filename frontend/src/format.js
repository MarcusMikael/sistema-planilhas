// Formatação em português (RNF-USA-02): datas dd/mm/aaaa e números com vírgula.

export function fmtData(iso) {
  if (!iso) return '—';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

export function fmtNum(n, casas = 1) {
  if (n === null || n === undefined) return '—';
  return Number(n).toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: 2 });
}

export function hojeIso() {
  const d = new Date();
  const p = (x) => String(x).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export const ROTULOS = {
  perfil: { RH: 'RH', GESTOR: 'Gestor', COLABORADOR: 'Colaborador' },
  tipoCompetencia: { COMPORTAMENTAL: 'Comportamental', TECNICA: 'Técnica' },
  tipoCompetenciaPlural: { COMPORTAMENTAL: 'Comportamentais', TECNICA: 'Técnicas' },
  statusCiclo: { RASCUNHO: 'Rascunho', LIBERADO: 'Em avaliação', RESULTADOS_LIBERADOS: 'Resultados liberados' },
  tipoAvaliacao: { AUTOAVALIACAO: 'Autoavaliação', GESTOR: 'Avaliação do gestor' },
  statusAvaliacao: { EM_ANDAMENTO: 'Em andamento', CONCLUIDA: 'Concluída' },
  nivel: { BAIXO: 'Baixo', MEDIO: 'Médio', ALTO: 'Alto' },
  statusPDI: { ATIVO: 'Ativo', CONCLUIDO: 'Concluído', CANCELADO: 'Cancelado' },
  statusAcao: { A_FAZER: 'A fazer', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído' },
  statusPesquisa: { RASCUNHO: 'Rascunho', PUBLICADA: 'Publicada', ENCERRADA: 'Encerrada' },
  tipoPergunta: { ESCALA: 'Escala 1 a 5', TEXTO: 'Texto livre', SETOR: 'Setor (automático)' },
};

export const ESCALA_CLIMA = [
  { valor: 1, rotulo: 'Muito insatisfeito(a)' },
  { valor: 2, rotulo: 'Insatisfeito(a)' },
  { valor: 3, rotulo: 'Indiferente' },
  { valor: 4, rotulo: 'Satisfeito(a)' },
  { valor: 5, rotulo: 'Muito satisfeito(a)' },
];

export const NIVEIS = ['BAIXO', 'MEDIO', 'ALTO'];

export function tomStatus(status) {
  switch (status) {
    case 'CONCLUIDA':
    case 'CONCLUIDO':
    case 'RESULTADOS_LIBERADOS':
    case 'PUBLICADA':
    case 'ATIVO':
      return 'ok';
    case 'EM_ANDAMENTO':
    case 'LIBERADO':
      return 'info';
    case 'CANCELADO':
    case 'ENCERRADA':
      return 'neutro';
    case 'ERRO':
      return 'erro';
    default:
      return 'aviso';
  }
}
