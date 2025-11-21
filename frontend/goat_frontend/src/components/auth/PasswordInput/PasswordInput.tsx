import React, { useState } from 'react';
import { Input, InputProps } from '../../common/Input/Input';
import styles from './PasswordInput.module.css';

export interface PasswordInputProps extends Omit<InputProps, 'type'> {
  showStrengthIndicator?: boolean;
}

export const PasswordInput: React.FC<PasswordInputProps> = ({
  showStrengthIndicator = false,
  ...inputProps
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [strength, setStrength] = useState<number>(0);

  const calculateStrength = (password: string): number => {
    if (!password) return 0;
    
    let strength = 0;
    if (password.length >= 8) strength += 1;
    if (/[a-z]/.test(password)) strength += 1;
    if (/[A-Z]/.test(password)) strength += 1;
    if (/[0-9]/.test(password)) strength += 1;
    if (/[^a-zA-Z0-9]/.test(password)) strength += 1;
    
    return strength;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (showStrengthIndicator) {
      setStrength(calculateStrength(e.target.value));
    }
    if (inputProps.onChange) {
      inputProps.onChange(e);
    }
  };

  const getStrengthLabel = (strength: number): string => {
    if (strength <= 1) return 'Muy débil';
    if (strength <= 2) return 'Débil';
    if (strength <= 3) return 'Media';
    if (strength <= 4) return 'Fuerte';
    return 'Muy fuerte';
  };

  const getStrengthColor = (strength: number): string => {
    if (strength <= 1) return '#ef4444';
    if (strength <= 2) return '#f59e0b';
    if (strength <= 3) return '#eab308';
    if (strength <= 4) return '#84cc16';
    return '#22c55e';
  };

  return (
    <div className={styles.container}>
      <div className={styles.inputWrapper}>
        <Input
          {...inputProps}
          type={showPassword ? 'text' : 'password'}
          onChange={handleChange}
        />
        <button
          type="button"
          className={styles.toggleButton}
          onClick={() => setShowPassword(!showPassword)}
          aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        >
          {showPassword ? '👁️' : '👁️‍🗨️'}
        </button>
      </div>
      {showStrengthIndicator && inputProps.value && (
        <div className={styles.strengthIndicator}>
          <div className={styles.strengthBar}>
            <div
              className={styles.strengthFill}
              style={{
                width: `${(strength / 5) * 100}%`,
                backgroundColor: getStrengthColor(strength),
              }}
            />
          </div>
          <span className={styles.strengthLabel}>
            {getStrengthLabel(strength)}
          </span>
        </div>
      )}
    </div>
  );
};

