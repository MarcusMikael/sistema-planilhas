import { Component } from 'react';
import { RotateCcw, TriangleAlert } from 'lucide-react';

/**
 * Captura erros de renderização para que uma falha em uma tela (ex.: um campo nulo inesperado vindo da API)
 * não deixe a aplicação inteira em branco. O Layout recria o boundary a cada troca de rota.
 */
export default class ErroBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { erro: null };
  }

  static getDerivedStateFromError(erro) {
    return { erro };
  }

  componentDidCatch(erro, info) {
    console.error('Erro ao renderizar a tela', erro, info?.componentStack);
  }

  render() {
    if (!this.state.erro) return this.props.children;
    return (
      <section className="pagina">
        <div className="cartao erro-tela" role="alert">
          <TriangleAlert size={28} strokeWidth={1.5} aria-hidden="true" />
          <h1>Algo deu errado nesta tela</h1>
          <p className="texto-2">
            Ocorreu um erro inesperado ao exibir esta página. Os seus dados já salvos não foram afetados.
          </p>
          <div className="linha">
            <button className="btn-primario" onClick={() => window.location.reload()}>
              <RotateCcw size={16} strokeWidth={1.5} aria-hidden="true" /> Recarregar a página
            </button>
            <a className="btn" href="/">Ir para o início</a>
          </div>
        </div>
      </section>
    );
  }
}
