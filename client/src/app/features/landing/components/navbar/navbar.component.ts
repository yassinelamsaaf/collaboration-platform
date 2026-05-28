import { ChangeDetectionStrategy, Component, DestroyRef, HostListener, inject } from '@angular/core';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { UserProfile } from '../../../../shared/models/auth.models';
import { mapHttpError } from '../../../../shared/utils/error-mapper';

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

  onLogout(): void {
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
          this.router.navigate(['/']);
        },
        error: (error: unknown) => {
          this.errorMessage = mapHttpError(error, 'Unable to log out.');
        }
      });
  }

  scrollToSection(id: string): void {
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
