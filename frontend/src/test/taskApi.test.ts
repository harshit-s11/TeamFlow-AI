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

  it('calls GET /api/v1/tasks/:id/activity', async () => {
    const mockActivities = [
      {
        id: 'log-1',
        projectId: 'proj-1',
        taskId: 'task-1',
        actorUserId: 'user-1',
        actorName: 'John Doe',
        eventType: 'TASK_CREATED',
        fieldChanged: null,
        oldValue: null,
        newValue: 'Task 1',
        createdAt: '2026-08-14T10:00:00Z',
      },
    ];
    vi.spyOn(apiClient, 'get').mockResolvedValueOnce({ data: mockActivities });

    const result = await taskApi.getTaskActivity('task-1');
    expect(result).toEqual(mockActivities);
    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/tasks/task-1/activity');
  });
});
