import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService } from '../../../../core/services/project.service';
import { TaskService } from '../../../../core/services/task.service';
import { ActivityLogService } from '../../../../core/services/activity-log.service';
import { ProjectResponse, ProjectMemberResponse } from '../../../../shared/models/project.models';
import { TaskResponse } from '../../../../shared/models/task.models';
import { TeamResponse } from '../../../../shared/models/team.models';
import { ActivityLogResponse } from '../../../../shared/models/activity-log.models';

@Component({
  selector: 'app-project-dashboard-page',
  templateUrl: './project-dashboard-page.component.html',
  styleUrl: './project-dashboard-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectDashboardPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);
  readonly projectService = inject(ProjectService);
  readonly taskService = inject(TaskService);
  readonly activityLogService = inject(ActivityLogService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  projectRef = '';
  project: ProjectResponse | null = null;
  members: ProjectMemberResponse[] = [];
  teams: TeamResponse[] = [];
  recentActivity: ActivityLogResponse[] = [];
  tasks: TaskResponse[] = [];
  activeTab = 'dashboard';

  ngOnInit(): void {
    this.projectRef = this.route.snapshot.paramMap.get('projectRef') || '';
    if (this.projectRef) this.loadProjectData();
  }

  loadProjectData(): void {
    this.projectService.get(this.projectRef)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (project) => {
        this.project = project;
        this.cdr.markForCheck();
      }});

    this.projectService.listMembers(this.projectRef, { page: 0, size: 50 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.members = res.content;
        this.cdr.markForCheck();
      }});

    this.projectService.listTeams(this.projectRef)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (teams) => {
        this.teams = teams;
        this.cdr.markForCheck();
        if (teams.length) this.loadTasks(teams[0].id);
      }});

    this.activityLogService.list(this.projectRef, { page: 0, size: 10 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.recentActivity = res.content;
        this.cdr.markForCheck();
      }});
  }

  private loadTasks(teamId: string): void {
    this.taskService.list(this.projectRef, teamId, { page: 0, size: 50 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.tasks = res.content;
        this.cdr.markForCheck();
      }});
  }

  get doneCount(): number { return this.tasks.filter(t => t.status === 'DONE').length; }
  get inProgressCount(): number { return this.tasks.filter(t => t.status === 'IN_PROGRESS' || t.status === 'IN_REVIEW').length; }
  get overdueCount(): number { return this.tasks.filter(t => t.status !== 'DONE' && t.dueDate && new Date(t.dueDate) < new Date()).length; }
  get completionPct(): number {
    if (!this.tasks.length) return 0;
    return Math.round((this.doneCount / this.tasks.length) * 100);
  }
  get donutCircumference(): number { return 2 * Math.PI * 50; }
  get donutDoneOffset(): number { return this.donutCircumference - (this.completionPct / 100) * this.donutCircumference; }
  get donutOverdueRotation(): number { return (this.doneCount / (this.tasks.length || 1)) * 360; }
  get donutOverdueOffset(): number { return this.donutCircumference - (this.overdueCount / (this.tasks.length || 1)) * this.donutCircumference; }

  getMemberInitials(userId: string): string {
    return userId.slice(0, 2).toUpperCase();
  }

  getTaskStatusIcon(task: TaskResponse): string {
    if (task.status === 'DONE') return 'check';
    if (task.status === 'IN_PROGRESS') return 'hourglass';
    return '';
  }
}
