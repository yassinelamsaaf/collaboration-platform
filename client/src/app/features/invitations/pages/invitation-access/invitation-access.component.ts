import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { AuthService } from '@core/services/auth.service';
import { UserProfile } from '@shared/models/auth.models';
import { ProjectInvitationPreview } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';

@Component({
  selector: 'app-invitation-access',
  templateUrl: './invitation-access.component.html',
  styleUrl: './invitation-access.component.scss',
  standalone: false
})
export class InvitationAccessComponent implements OnInit {
  token = '';
  preview: ProjectInvitationPreview | null = null;
  profile: UserProfile | null = null;
  loading = true;
  accepting = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly workspaceService: WorkspaceService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') ?? this.route.parent?.snapshot.paramMap.get('token') ?? '';
    if (!this.token) {
      this.loading = false;
      return;
    }

    this.workspaceService.previewInvitation(this.token).subscribe({
      next: (preview) => {
        this.preview = preview;
        this.authService.getProfile().subscribe((profile) => {
          this.profile = profile;
          this.loading = false;
        });
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get isSignedIn(): boolean {
    return !!this.profile;
  }

  get emailMatches(): boolean {
    return !!this.profile && !!this.preview && this.profile.email.toLowerCase() === this.preview.email.toLowerCase();
  }

  acceptInvitation(): void {
    if (!this.preview || !this.emailMatches) {
      return;
    }

    this.accepting = true;
    this.workspaceService.acceptInvitation(this.token).subscribe({
      next: () => {
        this.accepting = false;
        this.router.navigate(['/dashboard/projects', this.preview?.projectSlug || this.preview?.projectId]);
      },
      error: () => {
        this.accepting = false;
      }
    });
  }

  authQueryParams(): { email?: string; redirect: string } {
    return {
      email: this.preview?.email,
      redirect: `/invitations/${this.token}`
    };
  }
}
