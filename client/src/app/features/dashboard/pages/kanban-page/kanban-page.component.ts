import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskService } from '../../../../core/services/task.service';
import { ProjectService } from '../../../../core/services/project.service';
import { TaskResponse } from '../../../../shared/models/task.models';
import { TeamResponse } from '../../../../shared/models/team.models';

@Component({
  selector: 'app-kanban-page',
  templateUrl: './kanban-page.component.html',
  styleUrl: './kanban-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class KanbanPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);
  readonly taskService = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  projectRef = '';
  teams: TeamResponse[] = [];
  selectedTeamId = '';
  tasks: TaskResponse[] = [];
  drawerOpen = false;
  selectedTask: TaskResponse | null = null;

  readonly columns = [
    { key: 'TODO', label: 'TODO', color: 'var(--gray-500)' },
    { key: 'IN_PROGRESS', label: 'IN PROGRESS', color: 'var(--blue-500)' },
    { key: 'DONE', label: 'DONE', color: '#059669' }
  ];

  ngOnInit(): void {
    this.projectRef = this.route.snapshot.paramMap.get('projectRef') || '';
    if (this.projectRef) {
      this.projectService.listTeams(this.projectRef)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({ next: (res) => {
          this.teams = res.content;
          if (res.content.length) {
            this.selectedTeamId = res.content[0].id;
            this.loadTasks();
          }
          this.cdr.markForCheck();
        }});
    }
  }

  loadTasks(): void {
    if (!this.selectedTeamId) return;
    this.taskService.list(this.projectRef, this.selectedTeamId, { page: 0, size: 100 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.tasks = res.content;
        this.cdr.markForCheck();
      }});
  }

  getTasksByStatus(status: string): TaskResponse[] {
    return this.tasks.filter(t => t.status === status);
  }

  openDrawer(task: TaskResponse): void {
    this.selectedTask = task;
    this.drawerOpen = true;
  }

  closeDrawer(): void {
    this.drawerOpen = false;
    this.selectedTask = null;
  }

  updateStatus(taskId: string, newStatus: string): void {
    this.taskService.update(this.projectRef, this.selectedTeamId, taskId, { status: newStatus })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (updated) => {
        const idx = this.tasks.findIndex(t => t.id === taskId);
        if (idx !== -1) {
          this.tasks[idx] = updated;
          this.cdr.markForCheck();
        }
      }});
  }

  getPriorityClass(task: TaskResponse): string {
    if (task.priority === 'HIGH' || task.priority === 'URGENT') return 'priority-high';
    if (task.priority === 'MEDIUM') return 'priority-medium';
    return '';
  }

  getAssigneeInitials(id?: string): string {
    return id ? id.slice(0, 2).toUpperCase() : '?';
  }
}
