import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ToastService } from '@core/services/toast.service';
import { ProjectWorkspaceSnapshot, Team } from '@shared/models/workspace.models';
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

      this.loading = true;
      this.workspaceService.loadProjectWorkspace(projectRef).subscribe({
        next: (snapshot) => {
          this.snapshot = snapshot;
          this.loading = false;
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to load the project overview.'));
          this.loading = false;
        }
      });
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
