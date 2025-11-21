import { apiClient, handleApiError } from './api.config';
import {
  RegisterRequest,
  RegisterResponse,
  LoginRequest,
  LoginResponse,
  GenerateOtpRequest,
  GenerateOtpResponse,
  VerifyOtpRequest,
  VerifyOtpResponse,
  EmailConfirmationResponse,
} from '../types/auth.types';
import { ApiResponse } from '../types/api.types';

export const authService = {
  /**
   * Registra un nuevo usuario
   */
  register: async (data: RegisterRequest): Promise<ApiResponse<RegisterResponse>> => {
    try {
      const response = await apiClient.post<RegisterResponse>('/api/auth/register', data);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Inicia sesión con email y contraseña
   */
  login: async (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    try {
      const response = await apiClient.post<LoginResponse>('/api/auth/login', data);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Genera un código OTP para el propósito especificado
   */
  generateOTP: async (data: GenerateOtpRequest): Promise<ApiResponse<GenerateOtpResponse>> => {
    try {
      const response = await apiClient.post<GenerateOtpResponse>('/api/auth/otp', data);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Verifica un código OTP
   * El frontend llama al servicio Java, que actúa como proxy/intermediario con el servicio Python
   */
  verifyOTP: async (data: VerifyOtpRequest): Promise<ApiResponse<VerifyOtpResponse>> => {
    try {
      console.log('Llamando a verifyOTP a través de Java:', { endpoint: '/api/auth/verify', data });
      const response = await apiClient.post<VerifyOtpResponse>('/api/auth/verify', data);
      console.log('Respuesta de verifyOTP:', response.data);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      console.error('Error en verifyOTP:', error);
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Verifica el estado de confirmación de email
   */
  checkEmailConfirmation: async (
    email: string
  ): Promise<ApiResponse<EmailConfirmationResponse>> => {
    try {
      const response = await apiClient.get<EmailConfirmationResponse>(
        `/api/auth/confirm-email?email=${encodeURIComponent(email)}`
      );
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Flujo completo de registro con confirmación de email
   */
  registerWithEmailConfirmation: async (
    email: string,
    password: string
  ): Promise<ApiResponse<{ user: RegisterResponse; otpSent: boolean }>> => {
    try {
      const registerResult = await authService.register({ email, password });
      if (!registerResult.success || !registerResult.data) {
        return {
          success: false,
          error: registerResult.error || {
            status: 500,
            error: 'Error',
            message: 'Error al registrar usuario',
          },
        };
      }

      const otpResult = await authService.generateOTP({
        email,
        purpose: 'EMAIL_CONFIRMATION',
      });

      if (!otpResult.success || !otpResult.data?.success) {
        return {
          success: false,
          error: otpResult.error || {
            status: 500,
            error: 'Error',
            message: 'Error al generar código OTP',
          },
        };
      }

      return {
        success: true,
        data: {
          user: registerResult.data,
          otpSent: true,
        },
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },
};

