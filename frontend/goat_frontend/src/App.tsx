import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { RegisterPage } from './pages/auth/RegisterPage';
import { LoginPage } from './pages/auth/LoginPage';
import { VerifyOTPPage } from './pages/auth/VerifyOTPPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { Loading } from './components/common/Loading';
import './styles/globals.css';

// Componente para proteger rutas
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <Loading fullScreen message="Cargando..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// Componente para rutas públicas (redirige si ya está autenticado)
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <Loading fullScreen message="Cargando..." />;
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/register"
        element={
          <PublicRoute>
            <RegisterPage />
          </PublicRoute>
        }
      />
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/verify-otp"
        element={
          <PublicRoute>
            <VerifyOTPPage />
          </PublicRoute>
        }
      />
      <Route
        path="/reset-password"
        element={
          <PublicRoute>
            <ResetPasswordPage />
          </PublicRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

