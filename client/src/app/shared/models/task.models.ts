export interface TaskResponse {
  id: string;
  title: string;
  description?: string;
  projectId: string;
  teamId: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
  assigneeId?: string;
  assigneeName?: string;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
  subTaskCount?: number;
  completedSubTaskCount?: number;
  commentCount?: number;
  attachmentCount?: number;
  totalTimeMinutes?: number;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: string;
  status?: string;
  dueDate?: string;
  assigneeId?: string;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  priority?: string;
  status?: string;
  dueDate?: string;
  assigneeId?: string;
}

export interface SubTaskResponse {
  id: string;
  title: string;
  isDone: boolean;
  assigneeId?: string;
  taskId: string;
}

export interface CommentResponse {
  id: string;
  content: string;
  authorId: string;
  taskId: string;
  createdAt: string;
}

export interface AttachmentResponse {
  id: string;
  fileName: string;
  fileUrl: string;
  fileSize?: number;
  taskId: string;
  createdAt: string;
}

export interface TimeEntryResponse {
  id: string;
  durationMinutes: number;
  date: string;
  description?: string;
  taskId: string;
}

export interface LabelResponse {
  id: string;
  name: string;
  hexColor: string;
  projectId: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
