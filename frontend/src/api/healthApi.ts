import { apiClient } from './apiClient';

export interface HealthResponse {
  status: string;
}

export const healthApi = {
  async getHealth(): Promise<HealthResponse> {
    const response = await apiClient.get<HealthResponse>('/api/v1/health');
    return response.data;
  },
};
