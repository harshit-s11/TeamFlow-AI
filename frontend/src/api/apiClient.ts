import axios, { AxiosError } from 'axios';
import { tokenStorage } from '../utils/tokenStorage';
import { ApiErrorResponse } from '../types/api.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    if (error.response?.status === 401) {
      tokenStorage.clearToken();
    }
    return Promise.reject(error);
  }
);

export function parseApiError(error: unknown): string {
  if (axios.isAxiosError(error) && error.response?.data) {
    const apiError = error.response.data as ApiErrorResponse & {
      fieldErrors?: Array<{ field: string; message: string }>;
    };
    if (apiError.fieldErrors && apiError.fieldErrors.length > 0) {
      return apiError.fieldErrors.map((fe) => fe.message).join(', ');
    }
    if (apiError.message) {
      return apiError.message;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred. Please try again.';
}
