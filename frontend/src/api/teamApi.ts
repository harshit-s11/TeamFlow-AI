import { apiClient } from './apiClient';
import { TeamResponse } from '../types/domain.types';
import { UserResponse } from '../types/user.types';
import { TeamCreateRequest, TeamUpdateRequest } from '../types/team.types';

export const teamApi = {
  async getAllTeams(): Promise<TeamResponse[]> {
    const res = await apiClient.get<TeamResponse[]>('/api/v1/teams');
    return res.data;
  },

  async getTeamById(id: string): Promise<TeamResponse> {
    const res = await apiClient.get<TeamResponse>(`/api/v1/teams/${id}`);
    return res.data;
  },

  async createTeam(request: TeamCreateRequest): Promise<TeamResponse> {
    const res = await apiClient.post<TeamResponse>('/api/v1/teams', request);
    return res.data;
  },

  async updateTeam(id: string, request: TeamUpdateRequest): Promise<TeamResponse> {
    const res = await apiClient.put<TeamResponse>(`/api/v1/teams/${id}`, request);
    return res.data;
  },

  async deleteTeam(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/teams/${id}`);
  },

  async getTeamMembers(teamId: string): Promise<UserResponse[]> {
    const res = await apiClient.get<UserResponse[]>(`/api/v1/teams/${teamId}/members`);
    return res.data;
  },

  async addTeamMember(teamId: string, userId: string): Promise<UserResponse> {
    const res = await apiClient.post<UserResponse>(`/api/v1/teams/${teamId}/members`, { userId });
    return res.data;
  },

  async removeTeamMember(teamId: string, userId: string): Promise<void> {
    await apiClient.delete(`/api/v1/teams/${teamId}/members/${userId}`);
  },
};
