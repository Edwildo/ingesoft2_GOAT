import React from 'react';
import styles from './Loading.module.css';

export interface LoadingProps {
  size?: 'small' | 'medium' | 'large';
  fullScreen?: boolean;
  message?: string;
}

export const Loading: React.FC<LoadingProps> = ({
  size = 'medium',
  fullScreen = false,
  message,
}) => {
  const containerClasses = [
    styles.container,
    fullScreen && styles.fullScreen,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={containerClasses}>
      <div className={`${styles.spinner} ${styles[size]}`}></div>
      {message && <p className={styles.message}>{message}</p>}
    </div>
  );
};

