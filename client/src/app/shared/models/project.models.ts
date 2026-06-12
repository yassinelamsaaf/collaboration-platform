export interface ProjectResponse {
  id: string;
  name: string;
  description?: string;
  slug: string;
  status: 'ACTIVE' | 'ARCHIVED';
  startDate?: string;
  endDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
}

export interface ProjectMemberResponse {
  id: string;
  userId: string;
  projectId: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';
  joinedAt: string;
  memberName?: string;
  memberEmail?: string;
}

export interface InvitationResponse {
  id: string;
  email: string;
  role: string;
  status: string;
  token: string;
  createdAt: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
