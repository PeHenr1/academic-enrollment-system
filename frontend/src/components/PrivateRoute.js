import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const PrivateRoute = ({ children }) => {
  const { token } = useAuth();

  if (!token) {
    // Redireciona para o login se não estiver autenticado
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default PrivateRoute;