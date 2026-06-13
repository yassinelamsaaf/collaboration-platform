import { Component, OnInit } from '@angular/core';

import { AuthService } from '@core/services/auth.service';
import { DashboardSnapshot, NotificationItem, Task } from '@shared/models/workspace.models';
import { WorkspaceService } from './services/workspace.service';
import { ToastService } from '@core/services/toast.service';
import { mapHttpError } from '@shared/utils/error-mapper';
import {
  DONUT_CIRCUMFERENCE,
  DonutSegment,
  PriorityBar,
  donutSegmentsOf,
  priorityBarsOf
} from '@shared/utils/task-visuals';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  standalone: false
})
export class DashboardComponent implements OnInit {
  readonly circumference = DONUT_CIRCUMFERENCE;
  snapshot: DashboardSnapshot | null = null;
  loading = true;
  loadError: string | null = null;

  constructor(
    private readonly workspaceService: WorkspaceService,
    private readonly authService: AuthService,
    private readonly toast: ToastService
  ) {}

  ngOnInit(): void {
    this.authService.getProfile().subscribe({
      next: (profile) => this.loadSnapshot(profile?.id),
      error: () => this.loadSnapshot(undefined)
    });
  }

  private loadSnapshot(userId?: string): void {
    this.loading = true;
    this.workspaceService.loadDashboardSnapshot(userId).subscribe({
      next: (snapshot) => {
        this.snapshot = snapshot;
        this.loadError = null;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.loadError = mapHttpError(error, 'Unable to load the dashboard right now.');
        this.toast.warning('Dashboard overview could not be fully loaded. You can still continue from Projects.');
        this.loading = false;
      }
    });
  }

  get donutSegments(): DonutSegment[] {
    return this.snapshot ? donutSegmentsOf(this.snapshot.statusBreakdown, this.circumference) : [];
  }

  get priorityBars(): PriorityBar[] {
    return this.snapshot ? priorityBarsOf(this.snapshot.priorityBreakdown) : [];
  }

  dueLabel(task: Task): string {
    return task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date';
  }

  notificationTime(item: NotificationItem): string {
    return new Date(item.createdAt).toLocaleString();
  }
}
