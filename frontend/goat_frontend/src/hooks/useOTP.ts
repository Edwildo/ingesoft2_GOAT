import { useState, useCallback } from 'react';
import { authService } from '../api/auth.service';
import { OTPPurpose } from '../types/auth.types';
import { ApiResponse } from '../types/api.types';

export interface UseOTPReturn {
  otp: string;
  isLoading: boolean;
  error: string | null;
  expiresInMinutes: number | null;
  setOtp: (otp: string) => void;
  generateOTP: (email: string, purpose: OTPPurpose) => Promise<boolean>;
  verifyOTP: (email: string, purpose: OTPPurpose) => Promise<boolean>;
  reset: () => void;
}

export function useOTP(): UseOTPReturn {
  const [otp, setOtp] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expiresInMinutes, setExpiresInMinutes] = useState<number | null>(null);

  const generateOTP = useCallback(async (email: string, purpose: OTPPurpose): Promise<boolean> => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response: ApiResponse<{ success: boolean; message: string; expiresInMinutes: number | null }> = 
        await authService.generateOTP({ email, purpose });
      
      if (response.success && response.data) {
        setExpiresInMinutes(response.data.expiresInMinutes);
        return true;
      } else {
        setError(response.error?.message || 'Error al generar código OTP');
        return false;
      }
    } catch (err) {
      setError('Error de conexión con el servidor');
      return false;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const verifyOTP = useCallback(async (email: string, purpose: OTPPurpose): Promise<boolean> => {
    // Normalizar el OTP: solo números, exactamente 6 dígitos
    const normalizedOTP = otp.replace(/\D/g, '').slice(0, 6);
    
    if (!normalizedOTP || normalizedOTP.length !== 6) {
      setError('El código OTP debe tener 6 dígitos');
      return false;
    }

    setIsLoading(true);
    setError(null);
    
    try {
      console.log('Verificando OTP:', { email, otp: normalizedOTP, purpose });
      const response = await authService.verifyOTP({ email, otp: normalizedOTP, purpose });
      console.log('Respuesta del servidor:', response);
      
      if (response.success && response.data?.valid) {
        return true;
      } else {
        setError(response.error?.message || response.data?.message || 'Código OTP inválido');
        return false;
      }
    } catch (err) {
      console.error('Error al verificar OTP:', err);
      setError('Error de conexión con el servidor');
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [otp]);

  const reset = useCallback(() => {
    setOtp('');
    setError(null);
    setIsLoading(false);
    setExpiresInMinutes(null);
  }, []);

  return {
    otp,
    isLoading,
    error,
    expiresInMinutes,
    setOtp,
    generateOTP,
    verifyOTP,
    reset,
  };
}

