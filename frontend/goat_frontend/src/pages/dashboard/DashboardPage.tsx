import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import styles from './DashboardPage.module.css';

export const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className={styles.dashboardPage}>
      <header className={styles.dashboardHeader}>
        <div className={styles.headerContent}>
          <h1 className={styles.dashboardTitle}>GOAT</h1>
          <div className={styles.headerActions}>
            <span className={styles.userEmail}>{user?.email}</span>
            <Button variant="outline" size="small" onClick={handleLogout}>
              Cerrar Sesión
            </Button>
          </div>
        </div>
      </header>

      <main className={styles.dashboardContent}>
        <div className={styles.container}>
          <Card className={styles.welcomeCard}>
            <h2 className={styles.welcomeTitle}>Bienvenido a GOAT</h2>
            <p className={styles.welcomeText}>
              Has iniciado sesión exitosamente en el sistema de autenticación GOAT.
            </p>
            
            <div className={styles.userInfo}>
              <div className={styles.infoItem}>
                <strong>Email:</strong> {user?.email}
              </div>
              <div className={styles.infoItem}>
                <strong>Email Confirmado:</strong>{' '}
                {user?.emailConfirmed ? '✓ Sí' : '✗ No'}
              </div>
              <div className={styles.infoItem}>
                <strong>Estado:</strong>{' '}
                {user?.isActive ? '✓ Activo' : '✗ Inactivo'}
              </div>
              {user?.roles && user.roles.length > 0 && (
                <div className={styles.infoItem}>
                  <strong>Roles:</strong> {user.roles.join(', ')}
                </div>
              )}
            </div>
          </Card>
        </div>
      </main>

      <footer className={styles.dashboardFooter}>
        <p>GOAT Authentication System © 2025</p>
      </footer>
    </div>
  );
};

