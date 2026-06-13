import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ToastService } from '@core/services/toast.service';
import { ActivityLog, ProjectWorkspaceSnapshot, Team } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';
import {
  DONUT_CIRCUMFERENCE,
  DonutSegment,
  PriorityBar,
  completionRateOf,
  donutSegmentsOf,
  priorityBarsOf,
  priorityBreakdownOf,
  statusBreakdownOf
} from '@shared/utils/task-visuals';

interface TeamProgress {
  team: Team;
  total: number;
  done: number;
  percent: number;
}

@Component({
  selector: 'app-project-overview',
  templateUrl: './project-overview.component.html',
  styleUrl: './project-overview.component.scss',
  standalone: false
})
export class ProjectOverviewComponent implements OnInit {
  readonly circumference = DONUT_CIRCUMFERENCE;
  snapshot: ProjectWorkspaceSnapshot | null = null;
  loading = true;
  projectRef = '';
  showSettings = false;
  savingSettings = false;
  readonly settingsForm = { name: '', description: '' };

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
      this.load();
    });
  }

  private load(quiet = false): void {
    if (!quiet) {
      this.loading = true;
    }
    this.workspaceService.loadProjectWorkspace(this.projectRef).subscribe({
      next: (snapshot) => {
        this.snapshot = snapshot;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load the project overview.'));
        this.loading = false;
      }
    });
  }

  get canManageProject(): boolean {
    const role = this.snapshot?.project.currentUserRole;
    return role === 'OWNER' || role === 'ADMIN';
  }

  toggleSettings(): void {
    this.showSettings = !this.showSettings;
    if (this.showSettings && this.snapshot) {
      this.settingsForm.name = this.snapshot.project.name;
      this.settingsForm.description = this.snapshot.project.description ?? '';
    }
  }

  saveSettings(): void {
    if (!this.canManageProject || !this.settingsForm.name.trim()) {
      return;
    }
    this.savingSettings = true;
    this.workspaceService
      .updateProject(this.projectRef, {
        name: this.settingsForm.name.trim(),
        description: this.settingsForm.description.trim() || undefined
      })
      .subscribe({
        next: () => {
          this.savingSettings = false;
          this.showSettings = false;
          this.toast.success('Project updated.');
          this.load(true);
        },
        error: (error: unknown) => {
          this.savingSettings = false;
          this.toast.error(mapHttpError(error, 'Unable to update the project.'));
        }
      });
  }

  archiveProject(): void {
    if (!this.canManageProject || this.snapshot?.project.status === 'ARCHIVED') {
      return;
    }
    this.workspaceService.archiveProject(this.projectRef).subscribe({
      next: () => {
        this.toast.success('Project archived.');
        this.load(true);
      },
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to archive the project.'))
    });
  }

  get donutSegments(): DonutSegment[] {
    return this.snapshot ? donutSegmentsOf(statusBreakdownOf(this.snapshot.tasks), this.circumference) : [];
  }

  get priorityBars(): PriorityBar[] {
    return this.snapshot ? priorityBarsOf(priorityBreakdownOf(this.snapshot.tasks)) : [];
  }

  get completionRate(): number {
    return this.snapshot ? completionRateOf(statusBreakdownOf(this.snapshot.tasks)) : 0;
  }

  activitySearchQuery = '';
  activityPage = 0;
  readonly activityPageSize = 8;

  get filteredActivity(): ActivityLog[] {
    const q = this.activitySearchQuery.trim().toLowerCase();
    if (!q) {
      return this.snapshot?.activity ?? [];
    }
    return (this.snapshot?.activity ?? []).filter(
      (a) =>
        a.action.toLowerCase().includes(q) ||
        a.details.toLowerCase().includes(q) ||
        a.entityType.toLowerCase().includes(q)
    );
  }

  get totalActivityPages(): number {
    return Math.max(1, Math.ceil(this.filteredActivity.length / this.activityPageSize));
  }

  get paginatedActivity(): ActivityLog[] {
    const start = this.activityPage * this.activityPageSize;
    return this.filteredActivity.slice(start, start + this.activityPageSize);
  }

  prevActivityPage(): void {
    this.activityPage = Math.max(0, this.activityPage - 1);
  }

  nextActivityPage(): void {
    this.activityPage = Math.min(this.totalActivityPages - 1, this.activityPage + 1);
  }

  onActivityFilterChange(): void {
    this.activityPage = 0;
  }

  get teamProgress(): TeamProgress[] {
    if (!this.snapshot) {
      return [];
    }
    return this.snapshot.teams
      .map((team) => {
        const tasks = this.snapshot!.tasks.filter((task) => task.teamId === team.id);
        const done = tasks.filter((task) => task.status === 'DONE').length;
        return {
          team,
          total: tasks.length,
          done,
          percent: tasks.length ? Math.round((done / tasks.length) * 100) : 0
        };
      })
      .sort((a, b) => b.total - a.total);
  }
}
