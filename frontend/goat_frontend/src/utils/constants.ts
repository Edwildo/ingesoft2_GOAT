export const API_BASE_URL = 'http://localhost:8081'; // Servicio Java - Único punto de entrada

export const API_ENDPOINTS = {
  AUTH: {
    REGISTER: '/api/auth/register',
    LOGIN: '/api/auth/login',
    OTP: '/api/auth/otp',
    VERIFY: '/api/auth/verify', // Java actúa como proxy/intermediario con Python
    CONFIRM_EMAIL: '/api/auth/confirm-email',
  },
} as const;

export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_EMAIL: 'userEmail',
  USER_ID: 'userId',
  USER_DATA: 'userData',
} as const;

export const OTP_CONFIG = {
  LENGTH: 6,
  EXPIRY_MINUTES: 5,
} as const;

export const VALIDATION = {
  PASSWORD_MIN_LENGTH: 8,
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
} as const;

