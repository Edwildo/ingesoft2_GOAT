import React from 'react';
import styles from './Input.module.css';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, fullWidth = false, className = '', ...props }, ref) => {
    const inputClasses = [
      styles.input,
      error && styles.inputError,
      fullWidth && styles.fullWidth,
      className,
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <div className={`${styles.container} ${fullWidth ? styles.fullWidth : ''}`}>
        {label && (
          <label htmlFor={props.id} className={styles.label}>
            {label}
            {props.required && <span className={styles.required}>*</span>}
          </label>
        )}
        <input
          ref={ref}
          className={inputClasses}
          aria-invalid={!!error}
          aria-describedby={error ? `${props.id}-error` : undefined}
          {...props}
        />
        {error && (
          <span id={`${props.id}-error`} className={styles.error} role="alert">
            {error}
          </span>
        )}
        {helperText && !error && (
          <span className={styles.helperText}>{helperText}</span>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

