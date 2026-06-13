import { Component, OnInit } from '@angular/core';

import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@core/services/toast.service';
import { UserProfile } from '@shared/models/auth.models';
import { NotificationPrefs } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss',
  standalone: false
})
export class SettingsPageComponent implements OnInit {
  profile: UserProfile | null = null;
  username = '';
  savingProfile = false;
  prefs: NotificationPrefs = {
    emailOnTaskAssignment: false,
    emailOnStatusChange: false,
    emailOnCommentAdded: false,
    emailOnDeadlineApproaching: false,
    emailOnMemberInvited: false
  };
  savingPrefs = false;
  uploadingAvatar = false;

  constructor(
    private readonly authService: AuthService,
    private readonly workspaceService: WorkspaceService,
    private readonly toast: ToastService
  ) {}

  ngOnInit(): void {
    this.authService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.username = profile?.username ?? '';
      }
    });

    this.workspaceService.getNotificationPrefs().subscribe({
      next: (prefs) => (this.prefs = prefs),
      error: () => {}
    });
  }

  saveProfile(): void {
    const name = this.username.trim();
    if (!name || !this.profile) {
      return;
    }

    this.savingProfile = true;
    this.authService.updateProfile({ username: name }).subscribe({
      next: () => {
        this.toast.success('Profile updated.');
        this.profile = { ...this.profile!, username: name };
        this.savingProfile = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to update profile.'));
        this.savingProfile = false;
      }
    });
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.uploadingAvatar = true;
    this.authService.uploadAvatar(file).subscribe({
      next: () => {
        this.toast.success('Avatar uploaded.');
        this.uploadingAvatar = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to upload avatar.'));
        this.uploadingAvatar = false;
      }
    });
  }

  saveNotificationPrefs(): void {
    this.savingPrefs = true;
    this.workspaceService.updateNotificationPrefs(this.prefs).subscribe({
      next: () => {
        this.toast.success('Notification preferences saved.');
        this.savingPrefs = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to save notification preferences.'));
        this.savingPrefs = false;
      }
    });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        window.location.href = '/auth/login';
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to sign out cleanly.'));
      }
    });
  }
}
