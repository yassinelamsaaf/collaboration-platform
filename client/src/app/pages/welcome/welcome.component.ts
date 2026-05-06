import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AuthService } from '../../core/auth/auth.service';
import { mapHttpError } from '../../shared/helpers/error-mapper';
import { AlertComponent } from '../../shared/ui/alert/alert.component';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [NgIf, AlertComponent],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  loading = false;
  errorMessage = '';

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
        next: () => this.router.navigate(['/auth/login']),
        error: (error) => {
          this.errorMessage = mapHttpError(error, 'Unable to log out.');
        }
      });
  }
}
