export interface ApiError {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
}

export interface ApiResponse<T> {
  data?: T;
  error?: ApiError;
  success: boolean;
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

export interface RequestConfig {
  method?: HttpMethod;
  headers?: Record<string, string>;
  body?: unknown;
  requiresAuth?: boolean;
}

