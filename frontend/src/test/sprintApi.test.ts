import { describe, it, expect, vi } from 'vitest';
import { sprintApi } from '../api/sprintApi';
import { apiClient } from '../api/apiClient';

describe('sprintApi Service', () => {
  it('calls GET /api/v1/sprints', async () => {
    const mockSprints = [{ id: '1', name: 'Sprint 1', startDate: '2026-08-14', endDate: '2026-08-28', status: 'PLANNED' }];
    vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: mockSprints });

    const result = await sprintApi.getAllSprints();
    expect(result).toEqual(mockSprints);
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/sprints');
  });
});
