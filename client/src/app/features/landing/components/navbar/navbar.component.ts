import { ChangeDetectionStrategy, Component, DestroyRef, HostListener, inject } from '@angular/core';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import Swal from 'sweetalert2';

import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@core/services/toast.service';
import { UserProfile } from '@shared/models/auth.models';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-landing-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavbarComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toast = inject(ToastService);

  profile: UserProfile | null = null;
  menuOpen = false;
  mobileMenuOpen = false;
  loading = false;
  errorMessage = '';

  constructor() {
    this.authService.profile$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((profile) => {
      this.profile = profile;
    });

    this.authService.getProfile().pipe(takeUntilDestroyed(this.destroyRef)).subscribe();
  }

  get initials(): string {
    const value = this.profile?.username || this.profile?.email || '';
    if (!value) {
      return '';
    }

    const trimmed = value.trim();
    if (!trimmed) {
      return '';
    }

    const parts = trimmed.split(/\s+|@/).filter(Boolean);
    const first = parts[0]?.[0] ?? '';
    const second = parts[1]?.[0] ?? parts[0]?.[1] ?? '';
    return `${first}${second}`.toUpperCase();
  }

  get displayName(): string {
    return this.profile?.username || this.profile?.email || 'Profile';
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  onLogin(): void {
    this.router.navigate(['/auth/login']);
    this.mobileMenuOpen = false;
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
    this.menuOpen = false;
    this.mobileMenuOpen = false;
  }

  goToContactPage(): void {
    this.router.navigate(['/contact']);
    this.menuOpen = false;
    this.mobileMenuOpen = false;
  }

  onLogout(): void {
    void Swal.fire({
      title: 'Log out?',
      text: 'Are you sure you want to log out?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Log out',
      cancelButtonText: 'Cancel',
      buttonsStyling: false,
      customClass: {
        popup: 'swal-app-popup',
        title: 'swal-app-title',
        htmlContainer: 'swal-app-text',
        confirmButton: 'swal-app-confirm',
        cancelButton: 'swal-app-cancel'
      }
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      this.authService
        .logout()
        .pipe(
          finalize(() => (this.loading = false)),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe({
          next: () => {
            this.menuOpen = false;
            this.mobileMenuOpen = false;
            this.toast.neutral('Logged out successfully.');
            this.router.navigate(['/auth/login']);
          },
          error: (error: unknown) => {
            this.errorMessage = mapHttpError(error, 'Unable to log out.');
          }
        });
    });
  }

  scrollToSection(id: string): void {
    if (this.router.url !== '/') {
      this.router.navigate(['/']).then(() => {
        const element = document.getElementById(id);
        element?.scrollIntoView({ behavior: 'auto', block: 'start' });
      });
      this.closeMobileMenu();
      return;
    }

    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'auto', block: 'start' });
    }
    this.closeMobileMenu();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (!target?.closest('.profile-menu')) {
      this.menuOpen = false;
    }
  }
}
