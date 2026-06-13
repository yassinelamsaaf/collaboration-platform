import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ToastService } from '@core/services/toast.service';
import { mapHttpError } from '@shared/utils/error-mapper';
import { Priority, ProjectWorkspaceSnapshot, Task, TaskStatus, Team, TeamMember } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';

@Component({
  selector: 'app-project-kanban',
  templateUrl: './project-kanban.component.html',
  styleUrl: './project-kanban.component.scss',
  standalone: false
})
export class ProjectKanbanComponent implements OnInit {
  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];
  readonly priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

  projectRef = '';
  snapshot: ProjectWorkspaceSnapshot | null = null;
  loading = true;
  creating = false;
  showTaskForm = false;
  selectedTeamRef = 'all';
  focusedTaskId = '';
  draggedTask: Task | null = null;
  dragOverStatus: TaskStatus | null = null;
  readonly teamMembersByTeamId: Record<string, TeamMember[]> = {};

  readonly taskForm = {
    teamRef: '',
    title: '',
    description: '',
    priority: 'MEDIUM' as Priority,
    dueDate: '',
    assigneeId: ''
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly workspaceService: WorkspaceService,
    private readonly toast: ToastService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const projectRef = params.get('projectRef');
      if (!projectRef) {
        return;
      }

      this.projectRef = projectRef;
      this.loadWorkspace();
    });

    this.route.queryParamMap.subscribe((params) => {
      this.focusedTaskId = params.get('task') ?? '';
      this.selectedTeamRef = params.get('team') ?? this.selectedTeamRef;
    });
  }

  get canEditTasks(): boolean {
    const role = this.snapshot?.project.currentUserRole;
    return role === 'OWNER' || role === 'ADMIN' || role === 'MEMBER';
  }

  get projectRefForLinks(): string {
    return this.snapshot?.project.slug || this.snapshot?.project.id || this.projectRef;
  }

  loadWorkspace(quiet = false): void {
    if (!quiet) {
      this.loading = true;
    }
    this.workspaceService.loadProjectKanbanSnapshot(this.projectRef).subscribe({
      next: (snapshot) => {
        this.snapshot = snapshot;
        if (!this.taskForm.teamRef) {
          this.taskForm.teamRef = snapshot.teams[0]?.slug || snapshot.teams[0]?.id || '';
        }
        this.loadTeamMembers(snapshot.teams);
        this.loading = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load the Kanban board.'));
        this.loading = false;
      }
    });
  }

  // --- Drag & drop ---
  onDragStart(task: Task): void {
    if (this.canEditTasks) {
      this.draggedTask = task;
    }
  }

  onDragOver(event: DragEvent, status: TaskStatus): void {
    if (this.draggedTask) {
      event.preventDefault();
      this.dragOverStatus = status;
    }
  }

  onDragLeave(status: TaskStatus): void {
    if (this.dragOverStatus === status) {
      this.dragOverStatus = null;
    }
  }

  onDrop(status: TaskStatus): void {
    const task = this.draggedTask;
    this.draggedTask = null;
    this.dragOverStatus = null;
    if (task) {
      this.moveTask(task, status);
    }
  }

  tasksForStatus(status: TaskStatus): Task[] {
    return this.filteredTasks().filter((task) => task.status === status);
  }

  filteredTasks(): Task[] {
    const tasks = this.snapshot?.tasks ?? [];
    if (this.selectedTeamRef === 'all') {
      return tasks;
    }

    const selectedTeam = this.snapshot?.teams.find((team) => (team.slug || team.id) === this.selectedTeamRef);
    if (!selectedTeam) {
      return tasks;
    }

    return tasks.filter((task) => task.teamId === selectedTeam.id);
  }

  createTask(): void {
    if (!this.canEditTasks || !this.taskForm.teamRef || !this.taskForm.title.trim()) {
      return;
    }

    this.creating = true;
    this.workspaceService
      .createTask(this.projectRef, this.taskForm.teamRef, {
        title: this.taskForm.title.trim(),
        description: this.taskForm.description.trim() || undefined,
        priority: this.taskForm.priority,
        dueDate: this.taskForm.dueDate || undefined,
        assigneeId: this.taskForm.assigneeId || null
      })
      .subscribe({
        next: () => {
          this.taskForm.title = '';
          this.taskForm.description = '';
          this.taskForm.priority = 'MEDIUM';
          this.taskForm.dueDate = '';
          this.taskForm.assigneeId = '';
          this.creating = false;
          this.showTaskForm = false;
          this.toast.success('Task created successfully.');
          this.loadWorkspace(true);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to create the task.'));
          this.creating = false;
        }
      });
  }

  moveTask(task: Task, status: TaskStatus): void {
    if (!this.canEditTasks || task.status === status) {
      return;
    }

    const teamRef = this.teamRefByTask(task);
    if (!teamRef) {
      return;
    }

    // Optimistic update: move the card immediately so the board feels instant,
    // then reconcile with the server. Revert on failure.
    const previous = task.status;
    task.status = status;

    this.workspaceService.updateTaskStatus(this.projectRef, teamRef, task.id, status).subscribe({
      next: () => this.loadWorkspace(true),
      error: (error: unknown) => {
        task.status = previous;
        this.toast.error(mapHttpError(error, 'Unable to move the task.'));
      }
    });
  }

  teamRefByTask(task: Task): string {
    const team = this.snapshot?.teams.find((entry) => entry.id === task.teamId);
    // Fall back to the task's teamId UUID (the backend accepts UUID or slug as
    // teamRef) so the link never produces an empty, non-matching route segment.
    return team ? team.slug || team.id : task.teamId;
  }

  teamName(task: Task): string {
    return this.snapshot?.teams.find((team) => team.id === task.teamId)?.name ?? 'Unknown team';
  }

  isFocused(task: Task): boolean {
    return this.focusedTaskId === task.id;
  }

  taskRoute(task: Task): string[] {
    return ['/dashboard/projects', this.projectRefForLinks, 'tasks', this.teamRefByTask(task), task.id];
  }

  assigneeOptions(): { value: string; label: string }[] {
    const selectedTeam = this.teamOptions().find((team) => (team.slug || team.id) === this.taskForm.teamRef);
    if (!selectedTeam) {
      return [];
    }

    return (this.teamMembersByTeamId[selectedTeam.id] ?? []).map((member) => ({
      value: member.id,
      label: `${member.memberName} - ${member.role}`
    }));
  }

  teamOptions(): Team[] {
    return this.snapshot?.teams ?? [];
  }

  onTaskTeamChange(): void {
    this.taskForm.assigneeId = '';
  }

  private loadTeamMembers(teams: Team[]): void {
    teams.forEach((team) => {
      this.workspaceService.listTeamMembers(this.projectRef, team.slug || team.id).subscribe({
        next: (page) => {
          this.teamMembersByTeamId[team.id] = page.content;
        },
        error: () => {
          this.teamMembersByTeamId[team.id] = [];
        }
      });
    });
  }
}
