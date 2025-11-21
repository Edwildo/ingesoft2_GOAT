import React from 'react';
import styles from './Card.module.css';

export interface CardProps {
  children: React.ReactNode;
  title?: string;
  className?: string;
  padding?: 'none' | 'small' | 'medium' | 'large';
}

export const Card: React.FC<CardProps> = ({
  children,
  title,
  className = '',
  padding = 'medium',
}) => {
  const cardClasses = [
    styles.card,
    styles[`padding-${padding}`],
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={cardClasses}>
      {title && <h2 className={styles.title}>{title}</h2>}
      <div className={styles.content}>{children}</div>
    </div>
  );
};

