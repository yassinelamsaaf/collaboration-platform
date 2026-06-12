import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService } from '../../../../core/services/project.service';
import { ProjectResponse, ProjectMemberResponse } from '../../../../shared/models/project.models';
import { TeamResponse, TeamMemberResponse } from '../../../../shared/models/team.models';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-project-members-page',
  templateUrl: './project-members-page.component.html',
  styleUrl: './project-members-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectMembersPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  projectRef = '';
  project: ProjectResponse | null = null;
  members: ProjectMemberResponse[] = [];
  teams: TeamResponse[] = [];
  teamMembers: Record<string, TeamMemberResponse[]> = {};

  ngOnInit(): void {
    this.projectRef = this.route.snapshot.paramMap.get('projectRef') || '';
    if (this.projectRef) {
      this.loadData();
    }
  }

  loadData(): void {
    this.projectService.get(this.projectRef)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (p) => {
        this.project = p;
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
      .subscribe({ next: (res) => {
        this.teams = res.content;
        this.cdr.markForCheck();
        res.content.forEach(t => this.loadTeamMembers(t.id));
      }});
  }

  private loadTeamMembers(teamId: string): void {
    this.projectService.listTeamMembers(this.projectRef, teamId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.teamMembers[teamId] = res.content;
        this.cdr.markForCheck();
      }});
  }

  getTeamMembers(teamId: string): TeamMemberResponse[] {
    return this.teamMembers[teamId] || [];
  }

  getMemberInitials(userId: string): string {
    return userId.slice(0, 2).toUpperCase();
  }
}
