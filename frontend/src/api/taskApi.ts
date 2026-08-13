import { apiClient } from './apiClient';
import { TaskResponse } from '../types/domain.types';
import { TaskCreateRequest, TaskUpdateRequest } from '../types/task.types';

export const taskApi = {
  async getAllTasks(): Promise<TaskResponse[]> {
    const res = await apiClient.get<TaskResponse[]>('/api/v1/tasks');
    return res.data;
  },

  async getTaskById(id: string): Promise<TaskResponse> {
    const res = await apiClient.get<TaskResponse>(`/api/v1/tasks/${id}`);
    return res.data;
  },

  async createTask(request: TaskCreateRequest): Promise<TaskResponse> {
    const res = await apiClient.post<TaskResponse>('/api/v1/tasks', request);
    return res.data;
  },

  async updateTask(id: string, request: TaskUpdateRequest): Promise<TaskResponse> {
    const res = await apiClient.put<TaskResponse>(`/api/v1/tasks/${id}`, request);
    return res.data;
  },

  async deleteTask(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/tasks/${id}`);
  },

  async getTasksByProjectId(projectId: string): Promise<TaskResponse[]> {
    const res = await apiClient.get<TaskResponse[]>(`/api/v1/projects/${projectId}/tasks`);
    return res.data;
  },
};
