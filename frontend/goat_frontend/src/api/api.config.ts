import axios, { AxiosInstance, AxiosError } from 'axios';
import { API_BASE_URL } from '../utils/constants';
import { authStorage } from '../utils/storage';
import { ApiError } from '../types/api.types';

const createApiClient = (): AxiosInstance => {
  const client = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  });

  // Interceptor para agregar token a las peticiones
  client.interceptors.request.use(
    (config) => {
      const token = authStorage.getToken();
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Interceptor para manejar errores
  client.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      if (error.response?.status === 401) {
        // Token inválido o expirado
        authStorage.clear();
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return client;
};

export const apiClient = createApiClient();

export const handleApiError = (error: unknown): ApiError => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    if (axiosError.response?.data) {
      return axiosError.response.data;
    }
    return {
      status: axiosError.response?.status || 500,
      error: 'Error',
      message: axiosError.message || 'Error desconocido',
    };
  }
  return {
    status: 500,
    error: 'Error',
    message: 'Error desconocido',
  };
};

