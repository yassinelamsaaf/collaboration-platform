import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ToastService } from '@core/services/toast.service';
import { PageResponse, ProjectMember, ProjectWorkspaceSnapshot, Team, TeamMember, TeamRole } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-project-teams',
  templateUrl: './project-teams.component.html',
  styleUrl: './project-teams.component.scss',
  standalone: false
})
export class ProjectTeamsComponent implements OnInit {
  readonly teamRoles: TeamRole[] = ['LEADER', 'MEMBER'];
  readonly teamsPageSize = 4;

  projectRef = '';
  snapshot: ProjectWorkspaceSnapshot | null = null;
  selectedTeamId = '';
  loading = true;
  saving = false;
  showTeamForm = false;
  teamsLoading = false;
  teamsPage = 1;
  teamsPageInfo: PageResponse<Team> | null = null;
  teamSearchQuery = '';
  readonly snapshotTeamMembers: Record<string, TeamMember[]> = {};

  readonly teamForm = {
    name: '',
    description: ''
  };

  readonly teamMemberForm = {
    userId: '',
    role: 'MEMBER' as TeamRole
  };
  teamNameError = '';
  teamMemberError = '';

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
  }

  get canManageProject(): boolean {
    const role = this.snapshot?.project.currentUserRole;
    return role === 'OWNER' || role === 'ADMIN';
  }

  get selectedTeam(): Team | undefined {
    return this.snapshot?.teams.find((team) => team.id === this.selectedTeamId);
  }

  get selectedTeamMembers(): TeamMember[] {
    if (!this.selectedTeam) {
      return [];
    }

    return this.snapshotTeamMembers[this.selectedTeam.id] ?? [];
  }

  get teamsTotalPages(): number {
    return Math.max(1, this.teamsPageInfo?.totalPages ?? 1);
  }

  get teamsTotalElements(): number {
    return this.teamsPageInfo?.totalElements ?? this.snapshot?.teams.length ?? 0;
  }

  loadWorkspace(): void {
    this.loading = true;
    forkJoin({
      project: this.workspaceService.getProject(this.projectRef),
      membersPage: this.workspaceService.listProjectMembers(this.projectRef, 1, 100)
    }).subscribe({
      next: ({ project, membersPage }) => {
        this.snapshot = {
          project,
          members: membersPage.content,
          teams: [],
          invitations: [],
          activity: [],
          tasks: [],
          labels: []
        };
        this.loading = false;
        this.loadTeamsPage(true);
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load team management.'));
        this.loading = false;
      }
    });
  }

  loadTeamsPage(selectFirst = false): void {
    if (!this.projectRef) {
      return;
    }

    this.teamsLoading = true;
    this.workspaceService
      .listTeams(this.projectRef, this.teamsPage, this.teamsPageSize, this.teamSearchQuery)
      .subscribe({
        next: (page) => {
          this.teamsPageInfo = page;
          if (this.snapshot) {
            this.snapshot = { ...this.snapshot, teams: page.content };
          }

          const selectedStillVisible = page.content.some((team) => team.id === this.selectedTeamId);
          if (selectFirst || !selectedStillVisible) {
            this.selectedTeamId = page.content[0]?.id ?? '';
            this.teamMemberForm.userId = '';
            this.teamMemberForm.role = 'MEMBER';
          }

          this.loadTeamMembers(page.content);
          this.teamsLoading = false;
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to load teams.'));
          this.teamsLoading = false;
        }
      });
  }

  applyTeamSearch(): void {
    this.teamsPage = 1;
    this.loadTeamsPage(true);
  }

  clearTeamSearch(): void {
    this.teamSearchQuery = '';
    this.applyTeamSearch();
  }

  prevTeamsPage(): void {
    if (this.teamsPage <= 1) {
      return;
    }

    this.teamsPage -= 1;
    this.loadTeamsPage(true);
  }

  nextTeamsPage(): void {
    if (this.teamsPage >= this.teamsTotalPages) {
      return;
    }

    this.teamsPage += 1;
    this.loadTeamsPage(true);
  }

  createTeam(): void {
    if (!this.canManageProject || !this.teamForm.name.trim()) {
      this.teamNameError = 'Team name is required.';
      this.toast.warning('Please enter a team name.');
      return;
    }

    this.teamNameError = '';
    this.saving = true;
    this.workspaceService
      .createTeam(this.projectRef, {
        name: this.teamForm.name.trim(),
        description: this.teamForm.description.trim() || undefined
      })
      .subscribe({
        next: () => {
          this.teamForm.name = '';
          this.teamForm.description = '';
          this.teamNameError = '';
          this.saving = false;
          this.showTeamForm = false;
          this.teamsPage = 1;
          this.teamSearchQuery = '';
          this.toast.success('Team created successfully.');
          this.loadTeamsPage(true);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to create the team.'));
          this.saving = false;
        }
      });
  }

  selectTeam(teamId: string): void {
    this.selectedTeamId = teamId;
    this.teamMemberForm.userId = '';
    this.teamMemberForm.role = 'MEMBER';
    if (!this.snapshotTeamMembers[teamId]) {
      this.loadTeamMembersForId(teamId);
    }
  }

  addTeamMember(): void {
    if (!this.canManageProject || !this.selectedTeam || !this.teamMemberForm.userId) {
      this.teamMemberError = 'Select a project member first.';
      this.toast.warning('Please select a project member.');
      return;
    }

    this.teamMemberError = '';
    this.workspaceService
      .addTeamMember(this.projectRef, this.selectedTeam.slug || this.selectedTeam.id, {
        userId: this.teamMemberForm.userId,
        role: this.teamMemberForm.role
      })
      .subscribe({
        next: () => {
          this.teamMemberForm.userId = '';
          this.teamMemberForm.role = 'MEMBER';
          this.teamMemberError = '';
          this.toast.success(`Member added to ${this.selectedTeam?.name}.`);
          this.loadTeamMembersForId(this.selectedTeamId);
          this.loadTeamsPage(false);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to add the team member.'));
        }
      });
  }

  changeTeamMemberRole(member: TeamMember, role: TeamRole): void {
    if (!this.canManageProject || !this.selectedTeam || member.role === role) {
      return;
    }

    this.workspaceService
      .updateTeamMemberRole(this.projectRef, this.selectedTeam.slug || this.selectedTeam.id, member.userId, role)
      .subscribe({
        next: () => {
          this.toast.success(`Team role updated to ${role}.`);
          this.loadTeamMembersForId(this.selectedTeamId);
        },
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to update the team role.'))
      });
  }

  removeTeamMember(member: TeamMember): void {
    if (!this.canManageProject || !this.selectedTeam) {
      return;
    }

    this.workspaceService
      .removeTeamMember(this.projectRef, this.selectedTeam.slug || this.selectedTeam.id, member.userId)
      .subscribe({
        next: () => {
          this.toast.success('Team member removed.');
          this.loadTeamMembersForId(this.selectedTeamId);
          this.loadTeamsPage(false);
        },
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to remove the team member.'))
      });
  }

  teamMemberCandidates(): ProjectMember[] {
    const assignedIds = new Set(this.selectedTeamMembers.map((member) => member.userId));
    return (this.snapshot?.members ?? []).filter((member) => !assignedIds.has(member.userId));
  }

  teamSummary(team: Team): string {
    const count = team.memberCount ?? 0;
    if (count === 0) {
      return 'No participants yet';
    }
    return count === 1 ? '1 participant' : `${count} participants`;
  }

  private loadTeamMembers(teams: Team[]): void {
    this.snapshotTeamMembersReset();
    teams.forEach((team) => this.loadTeamMembersForId(team.id, team));
  }

  private loadTeamMembersForId(teamId: string, team?: Team): void {
    const resolvedTeam = team ?? this.snapshot?.teams.find((entry) => entry.id === teamId);
    if (!resolvedTeam) {
      return;
    }

    this.workspaceService.listTeamMembers(this.projectRef, resolvedTeam.slug || resolvedTeam.id).subscribe({
      next: (page) => {
        this.snapshotTeamMembers[teamId] = page.content;
      },
      error: () => {
        this.snapshotTeamMembers[teamId] = [];
      }
    });
  }

  private snapshotTeamMembersReset(): void {
    Object.keys(this.snapshotTeamMembers).forEach((key) => delete this.snapshotTeamMembers[key]);
  }
}
