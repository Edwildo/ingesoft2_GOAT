import React from 'react';
import { Input, InputProps } from '../../common/Input/Input';
import { validateEmail } from '../../../utils/validators';

export interface EmailInputProps extends Omit<InputProps, 'type' | 'onBlur'> {
  validateOnBlur?: boolean;
  onValidationChange?: (isValid: boolean) => void;
}

export const EmailInput: React.FC<EmailInputProps> = ({
  validateOnBlur = true,
  onValidationChange,
  value,
  error: externalError,
  ...inputProps
}) => {
  const [internalError, setInternalError] = React.useState<string | undefined>();

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    if (validateOnBlur && value) {
      const validation = validateEmail(value as string);
      if (!validation.valid) {
        setInternalError(validation.message);
        if (onValidationChange) {
          onValidationChange(false);
        }
      } else {
        setInternalError(undefined);
        if (onValidationChange) {
          onValidationChange(true);
        }
      }
    }
    
    if (inputProps.onBlur) {
      inputProps.onBlur(e);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    // Limpiar error cuando el usuario empieza a escribir
    if (internalError) {
      setInternalError(undefined);
      if (onValidationChange) {
        onValidationChange(true);
      }
    }
    
    if (inputProps.onChange) {
      inputProps.onChange(e);
    }
  };

  const error = externalError || internalError;

  return (
    <Input
      {...inputProps}
      type="email"
      value={value}
      error={error}
      onChange={handleChange}
      onBlur={handleBlur}
      autoComplete="email"
    />
  );
};

