import { apiClient } from './apiClient';
import { SprintResponse, TaskResponse } from '../types/domain.types';
import { SprintCreateRequest, SprintUpdateRequest } from '../types/sprint.types';

export const sprintApi = {
  async getAllSprints(): Promise<SprintResponse[]> {
    const res = await apiClient.get<SprintResponse[]>('/api/v1/sprints');
    return res.data;
  },

  async getSprintById(id: string): Promise<SprintResponse> {
    const res = await apiClient.get<SprintResponse>(`/api/v1/sprints/${id}`);
    return res.data;
  },

  async createSprint(request: SprintCreateRequest): Promise<SprintResponse> {
    const res = await apiClient.post<SprintResponse>('/api/v1/sprints', request);
    return res.data;
  },

  async updateSprint(id: string, request: SprintUpdateRequest): Promise<SprintResponse> {
    const res = await apiClient.put<SprintResponse>(`/api/v1/sprints/${id}`, request);
    return res.data;
  },

  async deleteSprint(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/sprints/${id}`);
  },

  async getTasksBySprintId(sprintId: string): Promise<TaskResponse[]> {
    const res = await apiClient.get<TaskResponse[]>(`/api/v1/sprints/${sprintId}/tasks`);
    return res.data;
  },

  async getSprintsByProjectId(projectId: string): Promise<SprintResponse[]> {
    const res = await apiClient.get<SprintResponse[]>(`/api/v1/projects/${projectId}/sprints`);
    return res.data;
  },
};
