import { apiClient } from './apiClient';
import { ProjectResponse } from '../types/domain.types';
import { UserResponse } from '../types/user.types';
import { ProjectCreateRequest, ProjectUpdateRequest } from '../types/project.types';

export const projectApi = {
  async getAllProjects(): Promise<ProjectResponse[]> {
    const res = await apiClient.get<ProjectResponse[]>('/api/v1/projects');
    return res.data;
  },

  async getProjectById(id: string): Promise<ProjectResponse> {
    const res = await apiClient.get<ProjectResponse>(`/api/v1/projects/${id}`);
    return res.data;
  },

  async createProject(request: ProjectCreateRequest): Promise<ProjectResponse> {
    const res = await apiClient.post<ProjectResponse>('/api/v1/projects', request);
    return res.data;
  },

  async updateProject(id: string, request: ProjectUpdateRequest): Promise<ProjectResponse> {
    const res = await apiClient.put<ProjectResponse>(`/api/v1/projects/${id}`, request);
    return res.data;
  },

  async deleteProject(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/projects/${id}`);
  },

  async getProjectMembers(projectId: string): Promise<UserResponse[]> {
    const res = await apiClient.get<UserResponse[]>(`/api/v1/projects/${projectId}/members`);
    return res.data;
  },

  async addProjectMember(projectId: string, userId: string): Promise<UserResponse> {
    const res = await apiClient.post<UserResponse>(`/api/v1/projects/${projectId}/members`, { userId });
    return res.data;
  },

  async removeProjectMember(projectId: string, userId: string): Promise<void> {
    await apiClient.delete(`/api/v1/projects/${projectId}/members/${userId}`);
  },
};
