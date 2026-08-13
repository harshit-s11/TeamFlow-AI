import { TaskStatus, TaskPriority } from './domain.types';

export interface TaskCreateRequest {
  projectId: string;
  sprintId?: string | null;
  assignedUserId?: string | null;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
}

export interface TaskUpdateRequest {
  sprintId?: string | null;
  assignedUserId?: string | null;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
}
