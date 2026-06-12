import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ProjectResponse, CreateProjectRequest, UpdateProjectRequest, ProjectMemberResponse, InvitationResponse, PaginatedResponse } from '../../shared/models/project.models';
import { TeamResponse, CreateTeamRequest, TeamMemberResponse } from '../../shared/models/team.models';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private readonly api: ApiService) {}

  list(params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<ProjectResponse>> {
    return this.api.get<PaginatedResponse<ProjectResponse>>('/projects', params);
  }

  get(ref: string): Observable<ProjectResponse> {
    return this.api.get<ProjectResponse>(`/projects/${ref}`);
  }

  create(body: CreateProjectRequest): Observable<ProjectResponse> {
    return this.api.post<ProjectResponse>('/projects', body);
  }

  update(ref: string, body: UpdateProjectRequest): Observable<ProjectResponse> {
    return this.api.patch<ProjectResponse>(`/projects/${ref}`, body);
  }

  archive(ref: string): Observable<ProjectResponse> {
    return this.api.post<ProjectResponse>(`/projects/${ref}/archive`, null);
  }

  listMembers(ref: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<ProjectMemberResponse>> {
    return this.api.get<PaginatedResponse<ProjectMemberResponse>>(`/projects/${ref}/members`, params);
  }

  updateMemberRole(ref: string, userId: string, role: string): Observable<void> {
    return this.api.patch<void>(`/projects/${ref}/members/${userId}/role`, { role });
  }

  removeMember(ref: string, userId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${ref}/members/${userId}`);
  }

  listInvitations(ref: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<InvitationResponse>> {
    return this.api.get<PaginatedResponse<InvitationResponse>>(`/projects/${ref}/invitations`, params);
  }

  createInvitation(ref: string, email: string): Observable<InvitationResponse> {
    return this.api.post<InvitationResponse>(`/projects/${ref}/invitations`, { email });
  }

  cancelInvitation(ref: string, invitationId: string): Observable<void> {
    return this.api.post<void>(`/projects/${ref}/invitations/${invitationId}/cancel`, null);
  }

  listTeams(ref: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<TeamResponse>> {
    return this.api.get<PaginatedResponse<TeamResponse>>(`/projects/${ref}/teams`, params);
  }

  createTeam(ref: string, body: CreateTeamRequest): Observable<TeamResponse> {
    return this.api.post<TeamResponse>(`/projects/${ref}/teams`, body);
  }

  updateTeam(ref: string, teamRef: string, body: CreateTeamRequest): Observable<TeamResponse> {
    return this.api.patch<TeamResponse>(`/projects/${ref}/teams/${teamRef}`, body);
  }

  deleteTeam(ref: string, teamRef: string): Observable<void> {
    return this.api.delete<void>(`/projects/${ref}/teams/${teamRef}`);
  }

  listTeamMembers(ref: string, teamRef: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<TeamMemberResponse>> {
    return this.api.get<PaginatedResponse<TeamMemberResponse>>(`/projects/${ref}/teams/${teamRef}/members`, params);
  }

  addTeamMember(ref: string, teamRef: string, userId: string): Observable<TeamMemberResponse> {
    return this.api.post<TeamMemberResponse>(`/projects/${ref}/teams/${teamRef}/members`, { userId });
  }

  updateTeamMemberRole(ref: string, teamRef: string, memberUserId: string, role: string): Observable<void> {
    return this.api.patch<void>(`/projects/${ref}/teams/${teamRef}/members/${memberUserId}/role`, { role });
  }

  removeTeamMember(ref: string, teamRef: string, memberUserId: string): Observable<void> {
    return this.api.delete<void>(`/projects/${ref}/teams/${teamRef}/members/${memberUserId}`);
  }
}
