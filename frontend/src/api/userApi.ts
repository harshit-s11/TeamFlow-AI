import { apiClient } from './apiClient';
import { UserResponse } from '../types/user.types';

export const userApi = {
  async getAllUsers(): Promise<UserResponse[]> {
    const res = await apiClient.get<UserResponse[]>('/api/v1/users');
    return res.data;
  },

  async getUserById(id: string): Promise<UserResponse> {
    const res = await apiClient.get<UserResponse>(`/api/v1/users/${id}`);
    return res.data;
  },
};
