import { lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Layout from './components/Layout.jsx';
import { Carregando } from './components/ui.jsx';
import Login from './pages/Login.jsx';
import Painel from './pages/Painel.jsx';

// Cada tela vira um arquivo separado, baixado só quando a rota é aberta
// (o colaborador não baixa as telas de cadastro do RH, por exemplo).
const Colaboradores = lazy(() => import('./pages/Colaboradores.jsx'));
const Usuarios = lazy(() => import('./pages/Usuarios.jsx'));
const Cadastros = lazy(() => import('./pages/Cadastros.jsx'));
const Competencias = lazy(() => import('./pages/Competencias.jsx'));
const Ciclos = lazy(() => import('./pages/Ciclos.jsx'));
const CicloDetalhe = lazy(() => import('./pages/CicloDetalhe.jsx'));
const Avaliacoes = lazy(() => import('./pages/Avaliacoes.jsx'));
const AvaliacaoForm = lazy(() => import('./pages/AvaliacaoForm.jsx'));
const Resultados = lazy(() => import('./pages/Resultados.jsx'));
const MapaCompetencias = lazy(() => import('./pages/MapaCompetencias.jsx'));
const NineBox = lazy(() => import('./pages/NineBox.jsx'));
const PDIs = lazy(() => import('./pages/PDIs.jsx'));
const PDIDetalhe = lazy(() => import('./pages/PDIDetalhe.jsx'));
const Clima = lazy(() => import('./pages/Clima.jsx'));
const ClimaResponder = lazy(() => import('./pages/ClimaResponder.jsx'));
const ClimaGestao = lazy(() => import('./pages/ClimaGestao.jsx'));
const ClimaResultados = lazy(() => import('./pages/ClimaResultados.jsx'));
const Historico = lazy(() => import('./pages/Historico.jsx'));
const Conta = lazy(() => import('./pages/Conta.jsx'));

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
