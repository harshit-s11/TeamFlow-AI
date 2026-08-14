import { describe, it, expect, vi } from 'vitest';
import { aiApi } from '../api/aiApi';
import { apiClient } from '../api/apiClient';

describe('aiApi Service', () => {
  it('calls POST /api/v1/ai/tasks/:id/breakdown', async () => {
    const mockBreakdown = {
      parentTaskId: 'task-1',
      suggestedSubtasks: [
        { title: 'Subtask 1', description: 'Desc 1', priority: 'HIGH', estimatedStoryPoints: 3 },
      ],
    };
    vi.spyOn(apiClient, 'post').mockResolvedValueOnce({ data: mockBreakdown });

    const result = await aiApi.generateTaskBreakdown('task-1', 4);
    expect(result).toEqual(mockBreakdown);
    expect(apiClient.post).toHaveBeenCalledWith('/api/v1/ai/tasks/task-1/breakdown', { targetSubtaskCount: 4 });
  });

  it('calls POST /api/v1/ai/sprints/:id/forecast', async () => {
    const mockForecast = {
      sprintId: 'sprint-1',
      historicalAverageVelocity: 20,
      plannedCapacity: 25,
      forecastedCompletionRate: 80,
      riskLevel: 'LOW',
      aiInsights: ['Good alignment.'],
    };
    vi.spyOn(apiClient, 'post').mockResolvedValueOnce({ data: mockForecast });

    const result = await aiApi.forecastSprintVelocity('sprint-1');
    expect(result).toEqual(mockForecast);
    expect(apiClient.post).toHaveBeenCalledWith('/api/v1/ai/sprints/sprint-1/forecast');
  });

  it('calls POST /api/v1/ai/projects/:id/standup-summary', async () => {
    const mockSummary = {
      projectId: 'proj-1',
      timeWindowHours: 24,
      completedWork: ['Task 1'],
      inProgressWork: ['Task 2'],
      blockersAndRisks: [],
      generatedSummary: 'Summary text',
    };
    vi.spyOn(apiClient, 'post').mockResolvedValueOnce({ data: mockSummary });

    const result = await aiApi.generateStandupSummary('proj-1', 24);
    expect(result).toEqual(mockSummary);
    expect(apiClient.post).toHaveBeenCalledWith('/api/v1/ai/projects/proj-1/standup-summary', { timeWindowHours: 24 });
  });
});
