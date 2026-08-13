import { describe, it, expect, vi } from 'vitest';
import { taskApi } from '../api/taskApi';
import { apiClient } from '../api/apiClient';

describe('taskApi Service', () => {
  it('calls GET /api/v1/tasks', async () => {
    const mockTasks = [{ id: '1', title: 'Task 1', status: 'TODO', priority: 'HIGH' }];
    vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: mockTasks });

    const result = await taskApi.getAllTasks();
    expect(result).toEqual(mockTasks);
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/tasks');
  });
});
