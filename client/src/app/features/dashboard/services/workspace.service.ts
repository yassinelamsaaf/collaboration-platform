import { Injectable } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap, timeout } from 'rxjs';

import { ApiService } from '@core/services/api.service';
import { MessageResponse } from '@shared/models/auth.models';
import {
  ActivityLog,
  Attachment,
  Comment,
  DashboardSnapshot,
  Label,
  NotificationItem,
  NotificationPrefs,
  PageResponse,
  Priority,
  PriorityBreakdown,
  Project,
  ProjectInvitation,
  ProjectInvitationPreview,
  ProjectMember,
  ProjectProgress,
  ProjectRole,
  ProjectWorkspaceSnapshot,
  SearchResultItem,
  StatusBreakdown,
  SubTask,
  Task,
  TaskStatus,
  Team,
  TeamMember,
  TeamRole,
  TimeEntry
} from '@shared/models/workspace.models';

interface ProjectTaskStats {
  project: Project;
  tasks: Task[];
  myMemberIds: Set<string>;
}

export interface TaskBundle {
  task: Task;
  comments: Comment[];
  attachments: Attachment[];
  subTasks: SubTask[];
  timeEntries: TimeEntry[];
  teamMembers: TeamMember[];
  projectMembers: ProjectMember[];
  labels: Label[];
}

@Injectable({ providedIn: 'root' })
export class WorkspaceService {
  constructor(private readonly api: ApiService) {}

  /**
   * Spring Data Pageable is 0-indexed on the request side; our components think
   * in 1-based pages. Convert here so the rest of the app stays 1-based and the
   * backend always receives the page index it expects.
   */
  private pageParams(page: number, size: number, extra?: Record<string, string | number | boolean | undefined>) {
    const zeroBased = Math.max(0, Math.floor(page) - 1);
    return { page: zeroBased, size, ...extra };
  }

  private emptyPage<T>(page = 1, size = 0): PageResponse<T> {
    return {
      content: [],
      page,
      size,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true
    };
  }

  private safePage<T>(source: Observable<PageResponse<T>>, page = 1, size = 100): Observable<PageResponse<T>> {
    return source.pipe(timeout(6000), catchError(() => of(this.emptyPage<T>(page, size))));
  }

  private safeArray<T>(source: Observable<T[]>): Observable<T[]> {
    return source.pipe(timeout(6000), catchError(() => of([])));
  }

  private safeValue<T>(source: Observable<T>, fallback: T): Observable<T> {
    return source.pipe(timeout(6000), catchError(() => of(fallback)));
  }

  listProjects(page = 1, size = 24): Observable<PageResponse<Project>> {
    return this.api.get<PageResponse<Project>>('projects', this.pageParams(page, size));
  }

  createProject(payload: { name: string; description?: string }): Observable<Project> {
    return this.api.post<Project>('projects', payload);
  }

  previewInvitation(token: string): Observable<ProjectInvitationPreview> {
    return this.api.get<ProjectInvitationPreview>(`projects/invitations/${token}`);
  }

  acceptInvitation(token: string): Observable<ProjectMember> {
    return this.api.post<ProjectMember>(`projects/invitations/${token}/accept`, {});
  }

  updateProject(projectRef: string, payload: { name?: string; description?: string }): Observable<Project> {
    return this.api.patch<Project>(`projects/${projectRef}`, payload);
  }

  archiveProject(projectRef: string): Observable<Project> {
    return this.api.post<Project>(`projects/${projectRef}/archive`, {});
  }

  getProject(projectRef: string): Observable<Project> {
    return this.api.get<Project>(`projects/${projectRef}`);
  }

  listProjectMembers(projectRef: string, page = 1, size = 100): Observable<PageResponse<ProjectMember>> {
    return this.api.get<PageResponse<ProjectMember>>(`projects/${projectRef}/members`, this.pageParams(page, size));
  }

  updateProjectMemberRole(projectRef: string, memberUserId: string, role: ProjectRole): Observable<ProjectMember> {
    return this.api.patch<ProjectMember>(`projects/${projectRef}/members/${memberUserId}/role`, { role });
  }

  removeProjectMember(projectRef: string, memberUserId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/members/${memberUserId}`);
  }

  listInvitations(projectRef: string, page = 1, size = 100): Observable<PageResponse<ProjectInvitation>> {
    return this.api.get<PageResponse<ProjectInvitation>>(`projects/${projectRef}/invitations`, this.pageParams(page, size));
  }

  inviteMember(projectRef: string, payload: { email: string; role: ProjectRole }): Observable<ProjectInvitation> {
    return this.api.post<ProjectInvitation>(`projects/${projectRef}/invitations`, payload);
  }

  cancelInvitation(projectRef: string, invitationId: string): Observable<MessageResponse> {
    return this.api.post<MessageResponse>(`projects/${projectRef}/invitations/${invitationId}/cancel`, {});
  }

  listTeams(projectRef: string, page = 1, size = 100): Observable<PageResponse<Team>> {
    return this.api.get<PageResponse<Team>>(`projects/${projectRef}/teams`, this.pageParams(page, size));
  }

  createTeam(projectRef: string, payload: { name: string; description?: string }): Observable<Team> {
    return this.api.post<Team>(`projects/${projectRef}/teams`, payload);
  }

  getTeam(projectRef: string, teamRef: string): Observable<Team> {
    return this.api.get<Team>(`projects/${projectRef}/teams/${teamRef}`);
  }

  updateTeam(projectRef: string, teamRef: string, payload: { name?: string; description?: string }): Observable<Team> {
    return this.api.patch<Team>(`projects/${projectRef}/teams/${teamRef}`, payload);
  }

  deleteTeam(projectRef: string, teamRef: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/teams/${teamRef}`);
  }

  listTeamMembers(projectRef: string, teamRef: string, page = 1, size = 100): Observable<PageResponse<TeamMember>> {
    return this.api.get<PageResponse<TeamMember>>(`projects/${projectRef}/teams/${teamRef}/members`, this.pageParams(page, size));
  }

  addTeamMember(projectRef: string, teamRef: string, payload: { userId: string; role: TeamRole }): Observable<TeamMember> {
    return this.api.post<TeamMember>(`projects/${projectRef}/teams/${teamRef}/members`, payload);
  }

  updateTeamMemberRole(
    projectRef: string,
    teamRef: string,
    memberUserId: string,
    role: TeamRole
  ): Observable<TeamMember> {
    return this.api.patch<TeamMember>(`projects/${projectRef}/teams/${teamRef}/members/${memberUserId}/role`, { role });
  }

  removeTeamMember(projectRef: string, teamRef: string, memberUserId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/teams/${teamRef}/members/${memberUserId}`);
  }

  listTasks(projectRef: string, teamRef: string, page = 1, size = 100): Observable<PageResponse<Task>> {
    return this.api.get<PageResponse<Task>>(`projects/${projectRef}/teams/${teamRef}/tasks`, this.pageParams(page, size));
  }

  createTask(
    projectRef: string,
    teamRef: string,
    payload: { title: string; description?: string; priority?: Priority; dueDate?: string; assigneeId?: string | null }
  ): Observable<Task> {
    return this.api.post<Task>(`projects/${projectRef}/teams/${teamRef}/tasks`, payload);
  }

  getTask(projectRef: string, teamRef: string, taskId: string): Observable<Task> {
    return this.api.get<Task>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}`);
  }

  updateTask(
    projectRef: string,
    teamRef: string,
    taskId: string,
    payload: { title?: string; description?: string; priority?: Priority; dueDate?: string; assigneeId?: string | null }
  ): Observable<Task> {
    return this.api.patch<Task>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}`, payload);
  }

  updateTaskStatus(projectRef: string, teamRef: string, taskId: string, status: TaskStatus): Observable<Task> {
    return this.api.patch<Task>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/status`, { status });
  }

  deleteTask(projectRef: string, teamRef: string, taskId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}`);
  }

  listLabels(projectRef: string): Observable<Label[]> {
    return this.api.get<Label[]>(`projects/${projectRef}/labels`);
  }

  createLabel(projectRef: string, payload: { name: string; color: string }): Observable<Label> {
    return this.api.post<Label>(`projects/${projectRef}/labels`, payload);
  }

  attachLabel(projectRef: string, labelId: string, taskId: string): Observable<MessageResponse> {
    return this.api.post<MessageResponse>(`projects/${projectRef}/labels/${labelId}/tasks/${taskId}`, {});
  }

  removeLabel(projectRef: string, labelId: string, taskId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/labels/${labelId}/tasks/${taskId}`);
  }

  listSubTasks(projectRef: string, teamRef: string, taskId: string): Observable<SubTask[]> {
    return this.api.get<SubTask[]>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks`);
  }

  createSubTask(
    projectRef: string,
    teamRef: string,
    taskId: string,
    payload: { title: string; assigneeId?: string | null }
  ): Observable<SubTask> {
    return this.api.post<SubTask>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks`, payload);
  }

  updateSubTask(
    projectRef: string,
    teamRef: string,
    taskId: string,
    subTaskId: string,
    payload: { title?: string; isDone?: boolean; assigneeId?: string | null }
  ): Observable<SubTask> {
    return this.api.patch<SubTask>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks/${subTaskId}`, payload);
  }

  deleteSubTask(projectRef: string, teamRef: string, taskId: string, subTaskId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/subtasks/${subTaskId}`);
  }

  listComments(projectRef: string, teamRef: string, taskId: string, page = 1, size = 100): Observable<PageResponse<Comment>> {
    return this.api.get<PageResponse<Comment>>(
      `projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/comments`,
      this.pageParams(page, size)
    );
  }

  createComment(projectRef: string, teamRef: string, taskId: string, content: string): Observable<Comment> {
    return this.api.post<Comment>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/comments`, { content });
  }

  deleteComment(projectRef: string, teamRef: string, taskId: string, commentId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/comments/${commentId}`);
  }

  listAttachments(projectRef: string, teamRef: string, taskId: string): Observable<Attachment[]> {
    return this.api.get<Attachment[]>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/attachments`);
  }

  createAttachment(
    projectRef: string,
    teamRef: string,
    taskId: string,
    payload: { fileName: string; fileUrl: string; fileSize: number }
  ): Observable<Attachment> {
    return this.api.post<Attachment>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/attachments`, payload);
  }

  deleteAttachment(
    projectRef: string,
    teamRef: string,
    taskId: string,
    attachmentId: string
  ): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(
      `projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/attachments/${attachmentId}`
    );
  }

  listTimeEntries(projectRef: string, teamRef: string, taskId: string, page = 1, size = 100): Observable<PageResponse<TimeEntry>> {
    return this.api.get<PageResponse<TimeEntry>>(
      `projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/time-entries`,
      this.pageParams(page, size)
    );
  }

  createTimeEntry(
    projectRef: string,
    teamRef: string,
    taskId: string,
    payload: { durationMinutes: number; date: string; description?: string }
  ): Observable<TimeEntry> {
    return this.api.post<TimeEntry>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/time-entries`, payload);
  }

  deleteTimeEntry(projectRef: string, teamRef: string, taskId: string, entryId: string): Observable<MessageResponse> {
    return this.api.delete<MessageResponse>(`projects/${projectRef}/teams/${teamRef}/tasks/${taskId}/time-entries/${entryId}`);
  }

  listNotifications(page = 1, size = 20, unreadOnly?: boolean): Observable<PageResponse<NotificationItem>> {
    return this.api.get<PageResponse<NotificationItem>>('notifications', this.pageParams(page, size, { unreadOnly }));
  }

  countUnreadNotifications(): Observable<number> {
    return this.api.get<number>('notifications/unread-count');
  }

  markNotificationAsRead(notificationId: string): Observable<NotificationItem> {
    return this.api.patch<NotificationItem>(`notifications/${notificationId}/read`, {});
  }

  markAllNotificationsAsRead(): Observable<void> {
    return this.api.patch<void>('notifications/read-all', {});
  }

  getNotificationPrefs(): Observable<NotificationPrefs> {
    return this.api.get<NotificationPrefs>('notifications/prefs');
  }

  updateNotificationPrefs(payload: NotificationPrefs): Observable<NotificationPrefs> {
    return this.api.put<NotificationPrefs>('notifications/prefs', payload);
  }

  listActivityLog(projectRef: string, page = 1, size = 30): Observable<PageResponse<ActivityLog>> {
    return this.api.get<PageResponse<ActivityLog>>(`projects/${projectRef}/activity-log`, this.pageParams(page, size));
  }

  /**
   * Builds the personal dashboard by fanning out across the user's projects →
   * teams → tasks and aggregating the result client-side (the backend has no
   * dedicated dashboard endpoint). Every call is individually guarded and the
   * whole pipeline is timeout-protected, so the dashboard always resolves to at
   * least the projects it managed to load instead of spinning forever.
   */
  loadDashboardSnapshot(myUserId?: string): Observable<DashboardSnapshot> {
    return forkJoin({
      projectsPage: this.safePage(this.listProjects(1, 50), 1, 50),
      notificationsPage: this.safePage(this.listNotifications(1, 8), 1, 8),
      unreadCount: this.safeValue(this.countUnreadNotifications(), 0)
    }).pipe(
      switchMap(({ projectsPage, notificationsPage }) => {
        const projects = projectsPage.content;
        const activeProjects = projects.filter((project) => project.status === 'ACTIVE');
        const scanned = activeProjects.slice(0, 8);

        const perProject = scanned.length
          ? forkJoin(scanned.map((project) => this.loadProjectTaskStats(project, myUserId)))
          : of([] as ProjectTaskStats[]);

        return perProject.pipe(
          map((statsList) => this.assembleDashboard(projects, projectsPage.totalElements, statsList, notificationsPage.content))
        );
      }),
      timeout(15000),
      catchError(() => of(this.emptyDashboard()))
    );
  }

  private loadProjectTaskStats(project: Project, myUserId?: string): Observable<ProjectTaskStats> {
    const ref = project.slug || project.id;
    return this.safePage(this.listTeams(ref)).pipe(
      switchMap((teamsPage) => {
        const teams = teamsPage.content;
        if (!teams.length) {
          return of({ project, tasks: [], myMemberIds: new Set<string>() } satisfies ProjectTaskStats);
        }

        const tasks$ = forkJoin(
          teams.map((team) => this.safePage(this.listTasks(ref, team.slug || team.id)).pipe(map((page) => page.content)))
        ).pipe(map((lists) => lists.flat()));

        const members$ = myUserId
          ? forkJoin(
              teams.map((team) => this.safePage(this.listTeamMembers(ref, team.slug || team.id)).pipe(map((page) => page.content)))
            ).pipe(map((lists) => lists.flat()))
          : of([] as TeamMember[]);

        return forkJoin({ tasks: tasks$, members: members$ }).pipe(
          map(({ tasks, members }) => ({
            project,
            tasks,
            myMemberIds: new Set(members.filter((member) => member.userId === myUserId).map((member) => member.id))
          }))
        );
      }),
      catchError(() => of({ project, tasks: [], myMemberIds: new Set<string>() } satisfies ProjectTaskStats))
    );
  }

  private assembleDashboard(
    projects: Project[],
    projectCount: number,
    statsList: ProjectTaskStats[],
    notifications: NotificationItem[]
  ): DashboardSnapshot {
    const status: StatusBreakdown = { TODO: 0, IN_PROGRESS: 0, IN_REVIEW: 0, DONE: 0 };
    const priority: PriorityBreakdown = { LOW: 0, MEDIUM: 0, HIGH: 0, URGENT: 0 };
    const now = new Date();
    const soon = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);

    let overdue = 0;
    let dueSoon = 0;
    const myTasks: Task[] = [];
    const upcoming: Task[] = [];
    const projectProgress: ProjectProgress[] = [];

    for (const { project, tasks, myMemberIds } of statsList) {
      let done = 0;
      for (const task of tasks) {
        status[task.status] = (status[task.status] ?? 0) + 1;
        priority[task.priority] = (priority[task.priority] ?? 0) + 1;
        if (task.status === 'DONE') {
          done += 1;
        }

        const due = task.dueDate ? new Date(task.dueDate) : null;
        if (due && task.status !== 'DONE') {
          if (due < now) {
            overdue += 1;
          } else if (due <= soon) {
            dueSoon += 1;
            upcoming.push(task);
          }
        }

        if (task.assigneeId && myMemberIds.has(task.assigneeId) && task.status !== 'DONE') {
          myTasks.push(task);
        }
      }
      projectProgress.push({
        project,
        total: tasks.length,
        done,
        percent: tasks.length ? Math.round((done / tasks.length) * 100) : 0
      });
    }

    const taskCount = status.TODO + status.IN_PROGRESS + status.IN_REVIEW + status.DONE;
    upcoming.sort((a, b) => (a.dueDate ?? '').localeCompare(b.dueDate ?? ''));

    return {
      projects,
      projectCount,
      activeProjectCount: projects.filter((project) => project.status === 'ACTIVE').length,
      taskCount,
      overdueCount: overdue,
      dueSoonCount: dueSoon,
      completedCount: status.DONE,
      inProgressCount: status.IN_PROGRESS + status.IN_REVIEW,
      myTaskCount: myTasks.length,
      completionRate: taskCount ? Math.round((status.DONE / taskCount) * 100) : 0,
      statusBreakdown: status,
      priorityBreakdown: priority,
      projectProgress: projectProgress.sort((a, b) => b.total - a.total),
      myTasks: myTasks.slice(0, 6),
      upcomingTasks: upcoming.slice(0, 6),
      recentNotifications: notifications
    };
  }

  private emptyDashboard(): DashboardSnapshot {
    return {
      projects: [],
      projectCount: 0,
      activeProjectCount: 0,
      taskCount: 0,
      overdueCount: 0,
      dueSoonCount: 0,
      completedCount: 0,
      inProgressCount: 0,
      myTaskCount: 0,
      completionRate: 0,
      statusBreakdown: { TODO: 0, IN_PROGRESS: 0, IN_REVIEW: 0, DONE: 0 },
      priorityBreakdown: { LOW: 0, MEDIUM: 0, HIGH: 0, URGENT: 0 },
      projectProgress: [],
      myTasks: [],
      upcomingTasks: [],
      recentNotifications: []
    };
  }

  loadProjectWorkspace(projectRef: string): Observable<ProjectWorkspaceSnapshot> {
    return forkJoin({
      project: this.getProject(projectRef).pipe(timeout(6000)),
      membersPage: this.safePage(this.listProjectMembers(projectRef)),
      teamsPage: this.safePage(this.listTeams(projectRef)),
      invitationsPage: this.safePage(this.listInvitations(projectRef)),
      activityPage: this.safePage(this.listActivityLog(projectRef), 1, 30),
      labels: this.safeArray(this.listLabels(projectRef))
    }).pipe(
      switchMap(({ project, membersPage, teamsPage, invitationsPage, activityPage, labels }) =>
        this.safeValue(this.listTasksAcrossTeams(projectRef, teamsPage.content), []).pipe(
          map((tasks) => ({
            project,
            members: membersPage.content,
            teams: teamsPage.content,
            invitations: invitationsPage.content,
            activity: activityPage.content,
            tasks,
            labels
          }))
        )
      )
    );
  }

  loadProjectStructureSnapshot(projectRef: string): Observable<ProjectWorkspaceSnapshot> {
    return forkJoin({
      project: this.getProject(projectRef).pipe(timeout(6000)),
      membersPage: this.safePage(this.listProjectMembers(projectRef)),
      teamsPage: this.safePage(this.listTeams(projectRef)),
      invitationsPage: this.safePage(this.listInvitations(projectRef))
    }).pipe(
      map(({ project, membersPage, teamsPage, invitationsPage }) => ({
        project,
        members: membersPage.content,
        teams: teamsPage.content,
        invitations: invitationsPage.content,
        activity: [],
        tasks: [],
        labels: []
      }))
    );
  }

  loadProjectKanbanSnapshot(projectRef: string): Observable<ProjectWorkspaceSnapshot> {
    return forkJoin({
      project: this.getProject(projectRef).pipe(timeout(6000)),
      teamsPage: this.safePage(this.listTeams(projectRef)),
      labels: this.safeArray(this.listLabels(projectRef))
    }).pipe(
      switchMap(({ project, teamsPage, labels }) =>
        this.safeValue(this.listTasksAcrossTeams(projectRef, teamsPage.content), []).pipe(
          map((tasks) => ({
            project,
            members: [],
            teams: teamsPage.content,
            invitations: [],
            activity: [],
            tasks,
            labels
          }))
        )
      )
    );
  }

  loadTaskBundle(projectRef: string, teamRef: string, taskId: string): Observable<TaskBundle> {
    // getTask is authoritative (a missing task must surface as an error); every
    // secondary resource is guarded so one failed call can't blank the page.
    return forkJoin({
      task: this.getTask(projectRef, teamRef, taskId).pipe(timeout(6000)),
      commentsPage: this.safePage(this.listComments(projectRef, teamRef, taskId)),
      attachments: this.safeArray(this.listAttachments(projectRef, teamRef, taskId)),
      subTasks: this.safeArray(this.listSubTasks(projectRef, teamRef, taskId)),
      timeEntriesPage: this.safePage(this.listTimeEntries(projectRef, teamRef, taskId)),
      teamMembersPage: this.safePage(this.listTeamMembers(projectRef, teamRef)),
      projectMembersPage: this.safePage(this.listProjectMembers(projectRef)),
      labels: this.safeArray(this.listLabels(projectRef))
    }).pipe(
      map(({ task, commentsPage, attachments, subTasks, timeEntriesPage, teamMembersPage, projectMembersPage, labels }) => ({
        task,
        comments: commentsPage.content,
        attachments,
        subTasks,
        timeEntries: timeEntriesPage.content,
        teamMembers: teamMembersPage.content,
        projectMembers: projectMembersPage.content,
        labels
      }))
    );
  }

  searchProjectContent(projectRef: string, query: string): Observable<SearchResultItem[]> {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
      return of([]);
    }

    return this.loadProjectWorkspace(projectRef).pipe(
      map((snapshot) => {
        const projectResults = [snapshot.project]
          .filter((project) => this.matches([project.name, project.description ?? ''], normalized))
          .map<SearchResultItem>((project) => ({
            kind: 'project',
            id: project.id,
            title: project.name,
            subtitle: project.description ?? 'Projet',
            meta: project.currentUserRole,
            route: `/dashboard/projects/${project.slug || project.id}`
          }));

        const teamResults = snapshot.teams
          .filter((team) => this.matches([team.name, team.description ?? ''], normalized))
          .map<SearchResultItem>((team) => ({
            kind: 'team',
            id: team.id,
            title: team.name,
            subtitle: team.description ?? 'Equipe',
            route: `/dashboard/projects/${projectRef}/teams`
          }));

        const memberResults = snapshot.members
          .filter((member) => this.matches([member.memberName, member.memberEmail, member.role], normalized))
          .map<SearchResultItem>((member) => ({
            kind: 'member',
            id: member.id,
            title: member.memberName,
            subtitle: member.memberEmail,
            meta: member.role,
            route: `/dashboard/projects/${projectRef}/teams`
          }));

        const taskResults = snapshot.tasks
          .filter((task) => this.matches([task.title, task.description ?? '', task.status, task.priority], normalized))
          .map<SearchResultItem>((task) => ({
            kind: 'task',
            id: task.id,
            title: task.title,
            subtitle: task.description ?? 'Tache',
            meta: `${task.status} · ${task.priority}`,
            route: `/dashboard/projects/${projectRef}/kanban?task=${task.id}`
          }));

        return [...projectResults, ...teamResults, ...memberResults, ...taskResults].slice(0, 40);
      })
    );
  }

  listTasksForProject(projectRef: string): Observable<Task[]> {
    return this.listTeams(projectRef).pipe(switchMap((teamsPage) => this.listTasksAcrossTeams(projectRef, teamsPage.content)));
  }

  private listTasksAcrossTeams(projectRef: string, teams: Team[]): Observable<Task[]> {
    if (teams.length === 0) {
      return of([]);
    }

    return forkJoin(teams.map((team) => this.listTasks(projectRef, team.slug || team.id))).pipe(
      map((pages) => pages.flatMap((page) => page.content))
    );
  }

  private matches(fields: string[], query: string): boolean {
    return fields.some((field) => field.toLowerCase().includes(query));
  }
}
