export type OTPPurpose = 'REGISTER' | 'LOGIN' | 'EMAIL_CONFIRMATION' | 'RESET_PASSWORD';

export interface RegisterRequest {
  email: string;
  password: string;
  roles?: string[];
}

export interface RegisterResponse {
  id: string;
  email: string;
  emailConfirmed: boolean;
  isActive: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  roles: string[];
  userId: string;
}

export interface GenerateOtpRequest {
  email: string;
  purpose: OTPPurpose;
}

export interface GenerateOtpResponse {
  success: boolean;
  message: string;
  expiresInMinutes: number | null;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
  purpose: OTPPurpose;
}

export interface VerifyOtpResponse {
  success: boolean;
  message: string;
  valid: boolean;
}

export interface EmailConfirmationResponse {
  email: string;
  confirmed: boolean;
  message: string;
}

export interface User {
  id: string;
  email: string;
  emailConfirmed: boolean;
  isActive: boolean;
  roles?: string[];
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

