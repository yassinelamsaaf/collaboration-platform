import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { TaskResponse, CreateTaskRequest, UpdateTaskRequest, SubTaskResponse, CommentResponse, AttachmentResponse, TimeEntryResponse, LabelResponse, PaginatedResponse } from '../../shared/models/task.models';

@Injectable({ providedIn: 'root' })
export class TaskService {
  constructor(private readonly api: ApiService) {}

  list(projectRef: string, teamRef: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<TaskResponse>> {
    return this.api.get<PaginatedResponse<TaskResponse>>(`/projects/${projectRef}/teams/${teamRef}/tasks`, params);
  }

  get(projectRef: string, teamRef: string, taskId: string): Observable<TaskResponse> {
    return this.api.get<TaskResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}`);
  }

  create(projectRef: string, teamRef: string, body: CreateTaskRequest): Observable<TaskResponse> {
    return this.api.post<TaskResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks`, body);
  }

  update(projectRef: string, teamRef: string, taskId: string, body: UpdateTaskRequest): Observable<TaskResponse> {
    return this.api.patch<TaskResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}`, body);
  }

  updateStatus(projectRef: string, teamRef: string, taskId: string, status: string): Observable<TaskResponse> {
    return this.api.patch<TaskResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/status`, { status });
  }

  delete(projectRef: string, teamRef: string, taskId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}`);
  }

  listLabels(projectRef: string): Observable<LabelResponse[]> {
    return this.api.get<LabelResponse[]>(`/projects/${projectRef}/labels`);
  }

  createLabel(projectRef: string, body: { name: string; hexColor: string }): Observable<LabelResponse> {
    return this.api.post<LabelResponse>(`/projects/${projectRef}/labels`, body);
  }

  deleteLabel(projectRef: string, labelId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${projectRef}/labels/${labelId}`);
  }

  assignLabel(projectRef: string, labelId: string, taskId: string): Observable<void> {
    return this.api.post<void>(`/projects/${projectRef}/labels/${labelId}/tasks/${taskId}`, null);
  }

  unassignLabel(projectRef: string, labelId: string, taskId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${projectRef}/labels/${labelId}/tasks/${taskId}`);
  }

  listSubtasks(projectRef: string, teamRef: string, taskId: string): Observable<SubTaskResponse[]> {
    return this.api.get<SubTaskResponse[]>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks`);
  }

  createSubtask(projectRef: string, teamRef: string, taskId: string, body: { title: string; assigneeId?: string }): Observable<SubTaskResponse> {
    return this.api.post<SubTaskResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks`, body);
  }

  updateSubtask(projectRef: string, teamRef: string, taskId: string, subTaskId: string, body: { title?: string; isDone?: boolean }): Observable<SubTaskResponse> {
    return this.api.patch<SubTaskResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks/${subTaskId}`, body);
  }

  deleteSubtask(projectRef: string, teamRef: string, taskId: string, subTaskId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks/${subTaskId}`);
  }

  listComments(projectRef: string, teamRef: string, taskId: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<CommentResponse>> {
    return this.api.get<PaginatedResponse<CommentResponse>>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/comments`, params);
  }

  createComment(projectRef: string, teamRef: string, taskId: string, content: string): Observable<CommentResponse> {
    return this.api.post<CommentResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/comments`, { content });
  }

  deleteComment(projectRef: string, teamRef: string, taskId: string, commentId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/comments/${commentId}`);
  }

  listTimeEntries(projectRef: string, teamRef: string, taskId: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<TimeEntryResponse>> {
    return this.api.get<PaginatedResponse<TimeEntryResponse>>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/time-entries`, params);
  }

  createTimeEntry(projectRef: string, teamRef: string, taskId: string, body: { durationMinutes: number; date: string; description?: string }): Observable<TimeEntryResponse> {
    return this.api.post<TimeEntryResponse>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/time-entries`, body);
  }

  deleteTimeEntry(projectRef: string, teamRef: string, taskId: string, entryId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/time-entries/${entryId}`);
  }
}
