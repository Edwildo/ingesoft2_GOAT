import React from 'react';
import styles from './Alert.module.css';

export interface AlertProps {
  children: React.ReactNode;
  variant?: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  onClose?: () => void;
  className?: string;
}

export const Alert: React.FC<AlertProps> = ({
  children,
  variant = 'info',
  title,
  onClose,
  className = '',
}) => {
  const alertClasses = [
    styles.alert,
    styles[variant],
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={alertClasses} role="alert">
      <div className={styles.content}>
        {title && <div className={styles.title}>{title}</div>}
        <div className={styles.message}>{children}</div>
      </div>
      {onClose && (
        <button
          className={styles.closeButton}
          onClick={onClose}
          aria-label="Cerrar"
        >
          ×
        </button>
      )}
    </div>
  );
};

