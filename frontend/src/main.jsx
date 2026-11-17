import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from './App.jsx';
import { AuthProvider } from './auth.jsx';
import { AvisoServidorLento } from './components/ui.jsx';
import './styles.css';

// Roteador de dados: necessário para o useBlocker (aviso de alterações não salvas).
// As rotas continuam declaradas em <Routes> dentro do App.
const router = createBrowserRouter([
  {
    path: '*',
    element: (
      <AuthProvider>
        <AvisoServidorLento />
        <App />
      </AuthProvider>
    ),
  },
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
