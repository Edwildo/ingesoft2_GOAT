import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../Button';
import styles from './Navbar.module.css';

export const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/shop');
  };

  const isSeller = user?.roles?.includes('SELLER');

  return (
    <nav className={styles.navbar}>
      <div className={styles.container}>
        <Link to="/shop" className={styles.logo}>
          <h1 className={styles.logoText}>GOAT</h1>
        </Link>

        <div className={styles.menu}>
          <Link to="/shop" className={styles.menuItem}>
            Shop
          </Link>

          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className={styles.menuItem}>
                Dashboard
              </Link>
              {isSeller && (
                <Link to="/seller/listings" className={styles.menuItem}>
                  Mis Listings
                </Link>
              )}
              <div className={styles.userMenu}>
                <span className={styles.userEmail}>{user?.email}</span>
                <Button variant="outline" size="small" onClick={handleLogout}>
                  Salir
                </Button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.menuItem}>
                Iniciar Sesión
              </Link>
              <Button variant="primary" size="small" onClick={() => navigate('/register')}>
                Registrarse
              </Button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

