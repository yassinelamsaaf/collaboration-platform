import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {
  readonly authService = inject(AuthService);
  readonly notificationService = inject(NotificationService);
  readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  notificationPanelOpen = false;
  profileDropdownOpen = false;

  ngOnInit(): void {
    this.notificationService.refreshUnreadCount();
  }

  get initials(): string {
    const profile = this.authService['profileSubject'].getValue();
    if (profile?.username) return profile.username.slice(0, 2).toUpperCase();
    if (profile?.email) return profile.email[0].toUpperCase();
    return '?';
  }

  get profileName(): string {
    const profile = this.authService['profileSubject'].getValue();
    return profile?.username || profile?.email || 'Profile';
  }

  toggleNotifications(): void {
    this.notificationPanelOpen = !this.notificationPanelOpen;
  }

  toggleProfileDropdown(): void {
    this.profileDropdownOpen = !this.profileDropdownOpen;
  }

  logout(): void {
    this.profileDropdownOpen = false;
    Swal.fire({
      title: 'Sign Out?',
      text: 'You will be redirected to the login page.',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sign Out',
      cancelButtonText: 'Cancel'
    }).then((result) => {
      if (result.isConfirmed) {
        this.authService.logout().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
          this.router.navigate(['/auth/login']);
        });
      }
    });
  }

  closePanels(): void {
    this.profileDropdownOpen = false;
    this.notificationPanelOpen = false;
  }

}
