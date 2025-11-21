import React, { useRef, useState, KeyboardEvent, ChangeEvent } from 'react';
import styles from './OTPInput.module.css';

export interface OTPInputProps {
  length?: number;
  value: string;
  onChange: (value: string) => void;
  onComplete?: (value: string) => void;
  disabled?: boolean;
  error?: string;
  autoFocus?: boolean;
}

export const OTPInput: React.FC<OTPInputProps> = ({
  length = 6,
  value,
  onChange,
  onComplete,
  disabled = false,
  error,
  autoFocus = true,
}) => {
  const [focusedIndex, setFocusedIndex] = useState<number | null>(autoFocus ? 0 : null);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const handleChange = (index: number, inputValue: string) => {
    // Solo permitir números
    const numericValue = inputValue.replace(/\D/g, '');
    
    if (numericValue.length > 1) {
      // Si se pega un valor, distribuir los dígitos
      const digits = numericValue.slice(0, length).split('');
      const newValue = digits.join('').padEnd(length, '');
      onChange(newValue);
      
      // Enfocar el último input con valor
      const lastIndex = Math.min(digits.length - 1, length - 1);
      inputRefs.current[lastIndex]?.focus();
      setFocusedIndex(lastIndex);
      
      if (newValue.length === length && onComplete) {
        onComplete(newValue);
      }
      return;
    }

    // Actualizar el valor en la posición específica
    const newValue = value.split('');
    newValue[index] = numericValue;
    const updatedValue = newValue.join('').slice(0, length);
    onChange(updatedValue);

    // Mover al siguiente input si hay un valor
    if (numericValue && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
      setFocusedIndex(index + 1);
    }

    // Llamar onComplete si se completó
    if (updatedValue.length === length && onComplete) {
      onComplete(updatedValue);
    }
  };

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !value[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
      setFocusedIndex(index - 1);
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
      setFocusedIndex(index - 1);
    } else if (e.key === 'ArrowRight' && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
      setFocusedIndex(index + 1);
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').replace(/\D/g, '');
    if (pastedData) {
      const digits = pastedData.slice(0, length).split('');
      const newValue = digits.join('').padEnd(length, '');
      onChange(newValue);
      
      const lastIndex = Math.min(digits.length - 1, length - 1);
      inputRefs.current[lastIndex]?.focus();
      setFocusedIndex(lastIndex);
      
      if (newValue.length === length && onComplete) {
        onComplete(newValue);
      }
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.inputsContainer}>
        {Array.from({ length }).map((_, index) => (
          <input
            key={index}
            ref={(el) => {
              inputRefs.current[index] = el;
            }}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={value[index] || ''}
            onChange={(e: ChangeEvent<HTMLInputElement>) => handleChange(index, e.target.value)}
            onKeyDown={(e: KeyboardEvent<HTMLInputElement>) => handleKeyDown(index, e)}
            onPaste={handlePaste}
            onFocus={() => setFocusedIndex(index)}
            onBlur={() => setFocusedIndex(null)}
            disabled={disabled}
            className={`${styles.input} ${focusedIndex === index ? styles.focused : ''} ${error ? styles.error : ''}`}
            aria-label={`Dígito ${index + 1} del código OTP`}
          />
        ))}
      </div>
      {error && <span className={styles.errorMessage} role="alert">{error}</span>}
    </div>
  );
};

