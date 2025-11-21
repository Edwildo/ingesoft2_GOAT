import { VALIDATION } from './constants';

export const validateEmail = (email: string): { valid: boolean; message?: string } => {
  if (!email) {
    return { valid: false, message: 'El email es requerido' };
  }
  
  if (!VALIDATION.EMAIL_REGEX.test(email)) {
    return { valid: false, message: 'El formato del email no es válido' };
  }
  
  return { valid: true };
};

export const validatePassword = (password: string): { valid: boolean; message?: string } => {
  if (!password) {
    return { valid: false, message: 'La contraseña es requerida' };
  }
  
  if (password.length < VALIDATION.PASSWORD_MIN_LENGTH) {
    return {
      valid: false,
      message: `La contraseña debe tener al menos ${VALIDATION.PASSWORD_MIN_LENGTH} caracteres`,
    };
  }
  
  return { valid: true };
};

export const validateOTP = (otp: string): { valid: boolean; message?: string } => {
  if (!otp) {
    return { valid: false, message: 'El código OTP es requerido' };
  }
  
  if (!/^\d+$/.test(otp)) {
    return { valid: false, message: 'El código OTP debe contener solo números' };
  }
  
  if (otp.length !== 6) {
    return { valid: false, message: 'El código OTP debe tener 6 dígitos' };
  }
  
  return { valid: true };
};

export const validateForm = <T extends Record<string, unknown>>(
  data: T,
  validators: Record<keyof T, (value: unknown) => { valid: boolean; message?: string }>
): { valid: boolean; errors: Partial<Record<keyof T, string>> } => {
  const errors: Partial<Record<keyof T, string>> = {};
  
  for (const [key, validator] of Object.entries(validators)) {
    const result = validator(data[key as keyof T]);
    if (!result.valid) {
      errors[key as keyof T] = result.message;
    }
  }
  
  return {
    valid: Object.keys(errors).length === 0,
    errors,
  };
};

