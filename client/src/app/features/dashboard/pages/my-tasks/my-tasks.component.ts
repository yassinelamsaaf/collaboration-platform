import { Component, OnInit } from '@angular/core';

import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@core/services/toast.service';
import { MyTaskItem, TaskStatus } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-my-tasks',
  templateUrl: './my-tasks.component.html',
  styleUrl: './my-tasks.component.scss',
  standalone: false
})
export class MyTasksComponent implements OnInit {
  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];
  items: MyTaskItem[] = [];
  loading = true;
  activeStatus: TaskStatus | 'ALL' = 'ALL';

  constructor(
    private readonly workspaceService: WorkspaceService,
    private readonly authService: AuthService,
    private readonly toast: ToastService
  ) {}

  ngOnInit(): void {
    this.authService.getProfile().subscribe({
      next: (profile) => this.load(profile?.id),
      error: () => this.load(undefined)
    });
  }

  private load(userId?: string): void {
    this.loading = true;
    this.workspaceService.loadMyTasks(userId).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load your tasks.'));
        this.loading = false;
      }
    });
  }

  get filtered(): MyTaskItem[] {
    if (this.activeStatus === 'ALL') {
      return this.items;
    }
    return this.items.filter((item) => item.task.status === this.activeStatus);
  }

  countFor(status: TaskStatus): number {
    return this.items.filter((item) => item.task.status === status).length;
  }

  taskRoute(item: MyTaskItem): string[] {
    return ['/dashboard/projects', item.projectRef, 'tasks', item.teamRef, item.task.id];
  }

  isOverdue(item: MyTaskItem): boolean {
    return !!item.task.dueDate && item.task.status !== 'DONE' && new Date(item.task.dueDate) < new Date();
  }

  dueLabel(item: MyTaskItem): string {
    return item.task.dueDate ? new Date(item.task.dueDate).toLocaleDateString() : 'No due date';
  }
}
