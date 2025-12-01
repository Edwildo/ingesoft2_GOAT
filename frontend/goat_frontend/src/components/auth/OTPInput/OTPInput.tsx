import React, {
  useRef,
  useState,
  useEffect,
  KeyboardEvent,
  ChangeEvent,
  FocusEvent,
} from "react";
import styles from "./OTPInput.module.css";

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
  const [focusedIndex, setFocusedIndex] = useState<number | null>(
    autoFocus ? 0 : null
  );
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Normalizar el valor para asegurar que siempre tenga la longitud correcta
  const normalizedValue = value.padEnd(length, "").slice(0, length);

  // Auto-focus en el primer input al montar
  useEffect(() => {
    if (autoFocus && inputRefs.current[0] && !disabled) {
      inputRefs.current[0]?.focus();
    }
  }, [autoFocus, disabled]);

  // Auto-completar cuando todos los dígitos están llenos
  useEffect(() => {
    const allFilled = normalizedValue.split("").every((char) => char !== "");
    if (allFilled && onComplete && !error) {
      const timer = setTimeout(() => {
        onComplete(normalizedValue);
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [normalizedValue, length, onComplete, error]);

  const handleChange = (index: number, inputValue: string) => {
    const numericValue = inputValue.replace(/\D/g, "");

    if (numericValue.length > 1) {
      const digits = numericValue.slice(0, length).split("");
      const newValue = digits.join("").padEnd(length, "");
      onChange(newValue);

      const lastIndex = Math.min(digits.length - 1, length - 1);
      setTimeout(() => {
        inputRefs.current[lastIndex]?.focus();
        setFocusedIndex(lastIndex);
      }, 0);
      return;
    }

    if (numericValue) {
      const newValueArray = normalizedValue.split("");
      newValueArray[index] = numericValue;
      const newValue = newValueArray.join("").slice(0, length);
      onChange(newValue);

      if (index < length - 1) {
        setTimeout(() => {
          inputRefs.current[index + 1]?.focus();
          setFocusedIndex(index + 1);
        }, 0);
      }
    } else {
      const newValueArray = normalizedValue.split("");
      newValueArray[index] = "";
      onChange(newValueArray.join(""));
    }
  };

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    const currentValue = normalizedValue[index];

    if (e.key === "Backspace") {
      e.preventDefault();
      if (currentValue) {
        const newValueArray = normalizedValue.split("");
        newValueArray[index] = "";
        onChange(newValueArray.join(""));
      } else if (index > 0) {
        const newValueArray = normalizedValue.split("");
        newValueArray[index - 1] = "";
        onChange(newValueArray.join(""));
        inputRefs.current[index - 1]?.focus();
        setFocusedIndex(index - 1);
      }
      return;
    }

    if (e.key === "Delete") {
      e.preventDefault();
      const newValueArray = normalizedValue.split("");
      newValueArray[index] = "";
      onChange(newValueArray.join(""));
      return;
    }

    if (e.key === "ArrowLeft" && index > 0) {
      e.preventDefault();
      inputRefs.current[index - 1]?.focus();
      setFocusedIndex(index - 1);
      return;
    }

    if (e.key === "ArrowRight" && index < length - 1) {
      e.preventDefault();
      inputRefs.current[index + 1]?.focus();
      setFocusedIndex(index + 1);
      return;
    }

    if (e.key === "Home") {
      e.preventDefault();
      inputRefs.current[0]?.focus();
      setFocusedIndex(0);
      return;
    }

    if (e.key === "End") {
      e.preventDefault();
      const chars = normalizedValue.split("");
      let lastFilledIndex = -1;
      for (let i = chars.length - 1; i >= 0; i--) {
        if (chars[i] !== "") {
          lastFilledIndex = i;
          break;
        }
      }
      const targetIndex =
        lastFilledIndex >= 0 ? lastFilledIndex + 1 : length - 1;
      inputRefs.current[Math.min(targetIndex, length - 1)]?.focus();
      setFocusedIndex(Math.min(targetIndex, length - 1));
      return;
    }

    if (e.key.length === 1 && /[0-9]/.test(e.key)) {
      return;
    }

    if (e.key.length === 1 && !/[0-9]/.test(e.key)) {
      e.preventDefault();
    }
  };

  const handleFocus = (index: number, e: FocusEvent<HTMLInputElement>) => {
    setFocusedIndex(index);
    e.target.select();
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text").replace(/\D/g, "");

    if (pastedData) {
      const digits = pastedData.slice(0, length).split("");
      const newValue = digits.join("").padEnd(length, "");
      onChange(newValue);

      const lastIndex = Math.min(digits.length - 1, length - 1);
      setTimeout(() => {
        inputRefs.current[lastIndex]?.focus();
        setFocusedIndex(lastIndex);
      }, 0);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.inputsContainer}>
        {Array.from({ length }).map((_, index) => {
          const inputValue = normalizedValue[index] || "";
          const isFocused = focusedIndex === index;
          const hasValue = !!inputValue;
          const isError = !!error;

          return (
            <input
              key={index}
              ref={(el) => {
                inputRefs.current[index] = el;
              }}
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              maxLength={1}
              value={inputValue}
              onChange={(e: ChangeEvent<HTMLInputElement>) =>
                handleChange(index, e.target.value)
              }
              onKeyDown={(e: KeyboardEvent<HTMLInputElement>) =>
                handleKeyDown(index, e)
              }
              onPaste={handlePaste}
              onFocus={(e: FocusEvent<HTMLInputElement>) =>
                handleFocus(index, e)
              }
              disabled={disabled}
              className={`${styles.input} ${isFocused ? styles.focused : ""} ${
                hasValue ? styles.filled : ""
              } ${isError ? styles.error : ""}`}
              aria-label={`Dígito ${index + 1} de ${length} del código OTP`}
              aria-invalid={isError}
              autoComplete="one-time-code"
            />
          );
        })}
      </div>
      {error && (
        <span className={styles.errorMessage} role="alert">
          {error}
        </span>
      )}
      {normalizedValue.split("").every((char) => char !== "") && !error && (
        <span className={styles.successIndicator} aria-label="Código completo">
          ✓
        </span>
      )}
    </div>
  );
};
