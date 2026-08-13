import { describe, it, expect, vi } from 'vitest';
import { projectApi } from '../api/projectApi';
import { apiClient } from '../api/apiClient';

describe('projectApi Service', () => {
  it('calls GET /api/v1/projects', async () => {
    const mockProjects = [{ id: '1', name: 'Project Beta', description: 'Desc', createdAt: '2026-08-14' }];
    vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: mockProjects });

    const result = await projectApi.getAllProjects();
    expect(result).toEqual(mockProjects);
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects');
  });
});
