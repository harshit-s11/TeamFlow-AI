export interface ProjectCreateRequest {
  name: string;
  description: string;
}

export interface ProjectUpdateRequest {
  name: string;
  description: string;
}

export interface AddProjectMemberRequest {
  userId: string;
}
