import { apiClient } from './apiClient';
import {
  AiTaskBreakdownResponse,
  SprintVelocityForecastResponse,
  StandupSummaryResponse,
} from '../types/ai.types';

export const aiApi = {
  async generateTaskBreakdown(taskId: string, targetSubtaskCount = 4): Promise<AiTaskBreakdownResponse> {
    const res = await apiClient.post<AiTaskBreakdownResponse>(
      `/api/v1/ai/tasks/${taskId}/breakdown`,
      { targetSubtaskCount }
    );
    return res.data;
  },

  async forecastSprintVelocity(sprintId: string): Promise<SprintVelocityForecastResponse> {
    const res = await apiClient.post<SprintVelocityForecastResponse>(
      `/api/v1/ai/sprints/${sprintId}/forecast`
    );
    return res.data;
  },

  async generateStandupSummary(projectId: string, timeWindowHours = 24): Promise<StandupSummaryResponse> {
    const res = await apiClient.post<StandupSummaryResponse>(
      `/api/v1/ai/projects/${projectId}/standup-summary`,
      { timeWindowHours }
    );
    return res.data;
  },
};
