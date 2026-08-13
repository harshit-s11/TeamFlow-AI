import { SprintStatus } from './domain.types';

export interface SprintCreateRequest {
  projectId: string;
  name: string;
  startDate: string;
  endDate: string;
  status: SprintStatus;
}

export interface SprintUpdateRequest {
  name: string;
  startDate: string;
  endDate: string;
  status: SprintStatus;
}
