import { apiClient } from './apiClient';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

export const authApi = {
  async register(request: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/api/v1/auth/register', request);
    return response.data;
  },

  async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/api/v1/auth/login', request);
    return response.data;
  },
};
