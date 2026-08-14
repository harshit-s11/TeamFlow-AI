import { TaskPriority } from './domain.types';

export interface SuggestedSubtask {
  title: string;
  description: string;
  priority: TaskPriority;
  estimatedStoryPoints: number;
}

export interface AiTaskBreakdownResponse {
  parentTaskId: string;
  suggestedSubtasks: SuggestedSubtask[];
}

export interface SprintVelocityForecastResponse {
  sprintId: string;
  historicalAverageVelocity: number;
  plannedCapacity: number;
  forecastedCompletionRate: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  aiInsights: string[];
}

export interface StandupSummaryRequest {
  timeWindowHours?: number;
}

export interface StandupSummaryResponse {
  projectId: string;
  timeWindowHours: number;
  completedWork: string[];
  inProgressWork: string[];
  blockersAndRisks: string[];
  generatedSummary: string;
}
