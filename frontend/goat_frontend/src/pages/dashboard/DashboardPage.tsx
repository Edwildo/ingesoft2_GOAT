import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  // Redirigir según el rol del usuario
  if (user?.roles?.includes('SELLER')) {
    return <Navigate to="/seller/listings" replace />;
  } else if (user?.roles?.includes('BUYER')) {
    return <Navigate to="/shop" replace />;
  }

  // Si no tiene roles específicos, mostrar página simple o redirigir a shop
  return <Navigate to="/shop" replace />;
};

