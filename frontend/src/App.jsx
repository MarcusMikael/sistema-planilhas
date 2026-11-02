import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Layout from './components/Layout.jsx';
import { Carregando } from './components/ui.jsx';
import Login from './pages/Login.jsx';
import Painel from './pages/Painel.jsx';
import Colaboradores from './pages/Colaboradores.jsx';
import Usuarios from './pages/Usuarios.jsx';
import Cadastros from './pages/Cadastros.jsx';
import Competencias from './pages/Competencias.jsx';
import Ciclos from './pages/Ciclos.jsx';
import CicloDetalhe from './pages/CicloDetalhe.jsx';
import Avaliacoes from './pages/Avaliacoes.jsx';
import AvaliacaoForm from './pages/AvaliacaoForm.jsx';
import Resultados from './pages/Resultados.jsx';
import MapaCompetencias from './pages/MapaCompetencias.jsx';
import NineBox from './pages/NineBox.jsx';
import PDIs from './pages/PDIs.jsx';
import PDIDetalhe from './pages/PDIDetalhe.jsx';
import Clima from './pages/Clima.jsx';
import ClimaResponder from './pages/ClimaResponder.jsx';
import ClimaGestao from './pages/ClimaGestao.jsx';
import ClimaResultados from './pages/ClimaResultados.jsx';
import Historico from './pages/Historico.jsx';
import Conta from './pages/Conta.jsx';

function SoRH({ children }) {
  const { isRH } = useAuth();
  return isRH ? children : <Navigate to="/" replace />;
}

/** Telas de gestão de pessoas: RH, gestores e quem tem equipe no cadastro. */
function SoGestao({ children }) {
  const { isRH, isGestor, usuario } = useAuth();
  return isRH || isGestor || usuario.ehGestorDeEquipe ? children : <Navigate to="/" replace />;
}

export default function App() {
  const { usuario, carregando } = useAuth();
  if (carregando) return <Carregando />;
  if (!usuario) {
    return (
      <Routes>
        <Route path="*" element={<Login />} />
      </Routes>
    );
  }
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Painel />} />
        <Route path="colaboradores" element={<SoGestao><Colaboradores /></SoGestao>} />
        <Route path="usuarios" element={<SoRH><Usuarios /></SoRH>} />
        <Route path="cadastros" element={<SoRH><Cadastros /></SoRH>} />
        <Route path="competencias" element={<SoRH><Competencias /></SoRH>} />
        <Route path="ciclos" element={<SoRH><Ciclos /></SoRH>} />
        <Route path="ciclos/:id" element={<SoRH><CicloDetalhe /></SoRH>} />
        <Route path="avaliacoes" element={<Avaliacoes />} />
        <Route path="avaliacoes/:id" element={<AvaliacaoForm />} />
        <Route path="resultados" element={<Resultados />} />
        <Route path="mapa-competencias" element={<SoGestao><MapaCompetencias /></SoGestao>} />
        <Route path="nine-box" element={<SoGestao><NineBox /></SoGestao>} />
        <Route path="pdis" element={<PDIs />} />
        <Route path="pdis/:id" element={<PDIDetalhe />} />
        <Route path="clima" element={<Clima />} />
        <Route path="clima/:id/responder" element={<ClimaResponder />} />
        <Route path="clima/:id/gestao" element={<SoRH><ClimaGestao /></SoRH>} />
        <Route path="clima/:id/resultados" element={<SoRH><ClimaResultados /></SoRH>} />
        <Route path="historico" element={<Historico />} />
        <Route path="conta" element={<Conta />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
