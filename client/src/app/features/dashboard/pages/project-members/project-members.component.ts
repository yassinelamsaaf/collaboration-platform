import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { ToastService } from '@core/services/toast.service';
import {
  PageResponse,
  ProjectInvitation,
  ProjectMember,
  ProjectRole,
  ProjectWorkspaceSnapshot,
  Team,
  TeamMember
} from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrl: './project-members.component.scss',
  standalone: false
})
export class ProjectMembersComponent implements OnInit {
  readonly projectRoles: ProjectRole[] = ['ADMIN', 'MEMBER', 'VIEWER'];
  readonly membersPageSize = 6;

  projectRef = '';
  snapshot: ProjectWorkspaceSnapshot | null = null;
  loading = true;
  saving = false;
  showInviteForm = false;
  memberSearchQuery = '';
  memberTeamFilter = '';
  membersPage = 0;
  readonly snapshotTeamMembers: Record<string, TeamMember[]> = {};

  readonly inviteForm = {
    email: '',
    role: 'MEMBER' as ProjectRole
  };
  inviteEmailError = '';

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

  get canManageRoles(): boolean {
    return this.snapshot?.project.currentUserRole === 'OWNER';
  }

  get filteredMembers(): ProjectMember[] {
    let members = this.snapshot?.members ?? [];

    if (this.memberTeamFilter) {
      if (this.memberTeamFilter === '__none__') {
        const inAnyTeam = new Set(
          Object.values(this.snapshotTeamMembers).flatMap((teamMembers) => teamMembers.map((member) => member.userId))
        );
        members = members.filter((member) => !inAnyTeam.has(member.userId));
      } else {
        const teamUserIds = this.userIdsByTeam[this.memberTeamFilter];
        if (teamUserIds) {
          members = members.filter((member) => teamUserIds.has(member.userId));
        }
      }
    }

    const query = this.memberSearchQuery.trim().toLowerCase();
    if (!query) {
      return members;
    }

    return members.filter(
      (member) =>
        member.memberName.toLowerCase().includes(query) ||
        member.memberEmail.toLowerCase().includes(query) ||
        member.role.toLowerCase().includes(query)
    );
  }

  get paginatedMembers(): ProjectMember[] {
    const start = this.membersPage * this.membersPageSize;
    return this.filteredMembers.slice(start, start + this.membersPageSize);
  }

  get totalMembersPages(): number {
    return Math.max(1, Math.ceil(this.filteredMembers.length / this.membersPageSize));
  }

  get memberTeamOptions(): { id: string; name: string }[] {
    const teams = (this.snapshot?.teams ?? []).map((team) => ({ id: team.id, name: team.name }));
    const totalMembers = this.snapshot?.members.length ?? 0;
    const inAnyTeam = new Set(
      Object.values(this.snapshotTeamMembers).flatMap((teamMembers) => teamMembers.map((member) => member.userId))
    );
    const noTeamCount = Math.max(0, totalMembers - inAnyTeam.size);

    return [{ id: '', name: 'All members' }, ...teams, { id: '__none__', name: `No team (${noTeamCount})` }];
  }

  private get userIdsByTeam(): Record<string, Set<string>> {
    const map: Record<string, Set<string>> = {};
    for (const [teamId, members] of Object.entries(this.snapshotTeamMembers)) {
      map[teamId] = new Set(members.map((member) => member.userId));
    }
    return map;
  }

  loadWorkspace(): void {
    this.loading = true;
    this.workspaceService
      .getProject(this.projectRef)
      .pipe(
        switchMap((project) => {
          const canSeeInvitations = project.currentUserRole === 'OWNER' || project.currentUserRole === 'ADMIN';

          return forkJoin({
            project: of(project),
            membersPage: this.workspaceService.listProjectMembers(this.projectRef, 1, 100),
            teamsPage: this.workspaceService.listTeams(this.projectRef, 1, 100),
            invitationsPage: canSeeInvitations
              ? this.workspaceService.listInvitations(this.projectRef, 1, 100)
              : of(this.emptyPage<ProjectInvitation>())
          });
        })
      )
      .subscribe({
        next: ({ project, membersPage, teamsPage, invitationsPage }) => {
          this.snapshot = {
            project,
            members: membersPage.content,
            teams: teamsPage.content,
            invitations: invitationsPage.content,
            activity: [],
            tasks: [],
            labels: []
          };
          this.loading = false;
          this.membersPage = 0;
          this.loadTeamMembers(teamsPage.content);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to load project members.'));
          this.loading = false;
        }
      });
  }

  inviteMember(): void {
    if (!this.canManageProject || !this.inviteForm.email.trim()) {
      this.inviteEmailError = 'Email address is required.';
      this.toast.warning('Please enter an email address.');
      return;
    }

    if (!this.isEmail(this.inviteForm.email.trim())) {
      this.inviteEmailError = 'Enter a valid email address.';
      this.toast.warning('Please enter a valid email address.');
      return;
    }

    this.inviteEmailError = '';
    this.saving = true;
    this.workspaceService
      .inviteMember(this.projectRef, {
        email: this.inviteForm.email.trim(),
        role: this.inviteForm.role
      })
      .subscribe({
        next: () => {
          this.inviteForm.email = '';
          this.inviteForm.role = 'MEMBER';
          this.inviteEmailError = '';
          this.saving = false;
          this.showInviteForm = false;
          this.toast.success('Invitation sent successfully.');
          this.loadWorkspace();
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to send the invitation.'));
          this.saving = false;
        }
      });
  }

  changeProjectRole(member: ProjectMember, role: ProjectRole): void {
    if (!this.canManageRoles || member.role === role) {
      return;
    }

    this.workspaceService.updateProjectMemberRole(this.projectRef, member.userId, role).subscribe({
      next: () => {
        this.toast.success(`Project role updated to ${role}.`);
        this.loadWorkspace();
      },
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to update the project role.'))
    });
  }

  removeProjectMember(member: ProjectMember): void {
    if (!this.canRemoveMember(member)) {
      return;
    }

    this.workspaceService.removeProjectMember(this.projectRef, member.userId).subscribe({
      next: () => {
        this.toast.success('Project member removed.');
        this.loadWorkspace();
      },
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to remove the project member.'))
    });
  }

  cancelInvitation(invitationId: string): void {
    if (!this.canManageProject) {
      return;
    }

    this.workspaceService.cancelInvitation(this.projectRef, invitationId).subscribe({
      next: () => {
        this.toast.success('Invitation cancelled.');
        this.loadWorkspace();
      },
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to cancel the invitation.'))
    });
  }

  canRemoveMember(member: ProjectMember): boolean {
    const currentRole = this.snapshot?.project.currentUserRole;
    if (!currentRole || member.role === 'OWNER') {
      return false;
    }

    return currentRole === 'OWNER' || currentRole === 'ADMIN';
  }

  pendingInvitationCount(): number {
    return (this.snapshot?.invitations ?? []).filter((invitation) => invitation.status === 'PENDING').length;
  }

  memberTeamNames(userId: string): string[] {
    const names: string[] = [];
    for (const team of this.snapshot?.teams ?? []) {
      const members = this.snapshotTeamMembers[team.id] ?? [];
      if (members.some((member) => member.userId === userId)) {
        names.push(team.name);
      }
    }
    return names;
  }

  onMemberFilterChange(): void {
    this.membersPage = 0;
  }

  prevMembersPage(): void {
    this.membersPage = Math.max(0, this.membersPage - 1);
  }

  nextMembersPage(): void {
    this.membersPage = Math.min(this.totalMembersPages - 1, this.membersPage + 1);
  }

  private loadTeamMembers(teams: Team[]): void {
    Object.keys(this.snapshotTeamMembers).forEach((key) => delete this.snapshotTeamMembers[key]);
    teams.forEach((team) => {
      this.workspaceService.listTeamMembers(this.projectRef, team.slug || team.id).subscribe({
        next: (page) => {
          this.snapshotTeamMembers[team.id] = page.content;
        },
        error: () => {
          this.snapshotTeamMembers[team.id] = [];
        }
      });
    });
  }

  private emptyPage<T>(): PageResponse<T> {
    return {
      content: [],
      page: 1,
      size: 0,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true
    };
  }

  private isEmail(value: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }
}
