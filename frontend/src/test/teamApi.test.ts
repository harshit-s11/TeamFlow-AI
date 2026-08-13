import { describe, it, expect, vi } from 'vitest';
import { teamApi } from '../api/teamApi';
import { apiClient } from '../api/apiClient';

describe('teamApi Service', () => {
  it('calls GET /api/v1/teams', async () => {
    const mockTeams = [{ id: '1', name: 'Team Alpha', createdAt: '2026-08-14' }];
    vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: mockTeams });

    const result = await teamApi.getAllTeams();
    expect(result).toEqual(mockTeams);
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/teams');
  });
});
