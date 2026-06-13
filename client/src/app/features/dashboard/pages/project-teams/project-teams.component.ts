import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ToastService } from '@core/services/toast.service';
import {
  ProjectMember,
  ProjectRole,
  ProjectWorkspaceSnapshot,
  Team,
  TeamMember,
  TeamRole
} from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-project-teams',
  templateUrl: './project-teams.component.html',
  styleUrl: './project-teams.component.scss',
  standalone: false
})
export class ProjectTeamsComponent implements OnInit {
  readonly projectRoles: ProjectRole[] = ['ADMIN', 'MEMBER', 'VIEWER'];
  readonly teamRoles: TeamRole[] = ['LEADER', 'MEMBER'];

  projectRef = '';
  snapshot: ProjectWorkspaceSnapshot | null = null;
  selectedTeamId = '';
  loading = true;
  saving = false;
  showTeamForm = false;
  showInviteForm = false;

  memberSearchQuery = '';
  memberTeamFilter = '';
  membersPage = 0;
  readonly membersPageSize = 4;

  private get userIdsByTeam(): Record<string, Set<string>> {
    const map: Record<string, Set<string>> = {};
    for (const [teamId, members] of Object.entries(this.snapshotTeamMembers)) {
      map[teamId] = new Set(members.map((m) => m.userId));
    }
    return map;
  }

  get filteredMembers(): ProjectMember[] {
    let members = this.snapshot?.members ?? [];

    const teamFilter = this.memberTeamFilter;
    if (teamFilter) {
      if (teamFilter === '__none__') {
        const inAnyTeam = new Set(
          Object.values(this.snapshotTeamMembers).flatMap((m) => m.map((tm) => tm.userId))
        );
        members = members.filter((m) => !inAnyTeam.has(m.userId));
      } else {
        const teamUserIds = this.userIdsByTeam[teamFilter];
        if (teamUserIds) {
          members = members.filter((m) => teamUserIds.has(m.userId));
        }
      }
    }

    const q = this.memberSearchQuery.trim().toLowerCase();
    if (q) {
      members = members.filter(
        (m) => m.memberName.toLowerCase().includes(q) || m.memberEmail.toLowerCase().includes(q)
      );
    }

    return members;
  }

  get totalMembersPages(): number {
    return Math.max(1, Math.ceil(this.filteredMembers.length / this.membersPageSize));
  }

  get paginatedMembers(): ProjectMember[] {
    const start = this.membersPage * this.membersPageSize;
    return this.filteredMembers.slice(start, start + this.membersPageSize);
  }

  prevMembersPage(): void {
    this.membersPage = Math.max(0, this.membersPage - 1);
  }

  nextMembersPage(): void {
    this.membersPage = Math.min(this.totalMembersPages - 1, this.membersPage + 1);
  }

  onMemberFilterChange(): void {
    this.membersPage = 0;
  }

  memberTeamNames(userId: string): string[] {
    const teams = this.snapshot?.teams ?? [];
    const names: string[] = [];
    for (const team of teams) {
      const teamMembers = this.snapshotTeamMembers[team.id];
      if (teamMembers?.some((tm) => tm.userId === userId)) {
        names.push(team.name);
      }
    }
    return names;
  }

  get memberTeamOptions(): { id: string; name: string }[] {
    const teams = (this.snapshot?.teams ?? []).map((t) => ({ id: t.id, name: t.name }));
    const totalMembers = this.snapshot?.members.length ?? 0;
    const inAnyTeam = new Set(
      Object.values(this.snapshotTeamMembers).flatMap((m) => m.map((tm) => tm.userId))
    );
    const noTeamCount = totalMembers - inAnyTeam.size;
    return [{ id: '', name: 'All members' }, ...teams, { id: '__none__', name: `No team (${noTeamCount})` }];
  }

  readonly inviteForm = {
    email: '',
    role: 'MEMBER' as ProjectRole
  };

  readonly teamForm = {
    name: '',
    description: ''
  };

  readonly teamMemberForm = {
    userId: '',
    role: 'MEMBER' as TeamRole
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
  }

  get canManageProject(): boolean {
    const role = this.snapshot?.project.currentUserRole;
    return role === 'OWNER' || role === 'ADMIN';
  }

  get canManageRoles(): boolean {
    return this.snapshot?.project.currentUserRole === 'OWNER';
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

  readonly snapshotTeamMembers: Record<string, TeamMember[]> = {};

  loadWorkspace(): void {
    this.loading = true;
    this.workspaceService.loadProjectStructureSnapshot(this.projectRef).subscribe({
      next: (snapshot) => {
        this.snapshot = snapshot;
        this.selectedTeamId = this.pickSelectedTeam(snapshot);
        this.loadTeamMembers(snapshot.teams);
        this.loading = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load the project structure.'));
        this.loading = false;
      }
    });
  }

  createTeam(): void {
    if (!this.canManageProject || !this.teamForm.name.trim()) {
      return;
    }

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
          this.saving = false;
          this.showTeamForm = false;
          this.toast.success('Team created successfully.');
          this.loadWorkspace();
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to create the team.'));
          this.saving = false;
        }
      });
  }

  inviteMember(): void {
    if (!this.canManageProject || !this.inviteForm.email.trim()) {
      return;
    }

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

  selectTeam(teamId: string): void {
    this.selectedTeamId = teamId;
    if (!this.snapshotTeamMembers[teamId]) {
      this.loadTeamMembersForId(teamId);
    }
  }

  addTeamMember(): void {
    if (!this.canManageProject || !this.selectedTeam || !this.teamMemberForm.userId) {
      return;
    }

    this.workspaceService
      .addTeamMember(this.projectRef, this.selectedTeam.slug || this.selectedTeam.id, {
        userId: this.teamMemberForm.userId,
        role: this.teamMemberForm.role
      })
      .subscribe({
        next: () => {
          this.teamMemberForm.userId = '';
          this.teamMemberForm.role = 'MEMBER';
          this.toast.success(`Member added to ${this.selectedTeam?.name}.`);
          this.loadTeamMembersForId(this.selectedTeamId);
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
        },
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to remove the team member.'))
      });
  }

  teamMemberCandidates(): ProjectMember[] {
    const assignedIds = new Set(this.selectedTeamMembers.map((member) => member.userId));
    return (this.snapshot?.members ?? []).filter((member) => !assignedIds.has(member.userId));
  }

  canRemoveMember(member: ProjectMember): boolean {
    const currentRole = this.snapshot?.project.currentUserRole;
    if (!currentRole) {
      return false;
    }

    if (member.role === 'OWNER') {
      return false;
    }

    return currentRole === 'OWNER' || currentRole === 'ADMIN';
  }

  pendingInvitationCount(): number {
    return (this.snapshot?.invitations ?? []).filter((invitation) => invitation.status === 'PENDING').length;
  }

  private pickSelectedTeam(snapshot: ProjectWorkspaceSnapshot): string {
    if (snapshot.teams.some((team) => team.id === this.selectedTeamId)) {
      return this.selectedTeamId;
    }

    return snapshot.teams[0]?.id ?? '';
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
