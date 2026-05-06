import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { getControlError, markAllTouched } from '../../../shared/helpers/form-helpers';
import { mapHttpError } from '../../../shared/helpers/error-mapper';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgIf, AlertComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });
  loading = false;
  errorMessage = '';
  successMessage = '';

  onSubmit(): void {
    if (this.form.invalid) {
      markAllTouched(this.form);
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService
      .login(this.form.getRawValue())
      .pipe(
        finalize(() => (this.loading = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.successMessage = 'Signed in successfully. Redirecting...';
          window.setTimeout(() => this.router.navigate(['/welcome']), 700);
        },
        error: (error) => {
          this.errorMessage = mapHttpError(error, 'Unable to sign in. Please try again.');
        }
      });
  }

  get emailError(): string | null {
    return getControlError(this.form.get('email'), 'Email');
  }

  get passwordError(): string | null {
    return getControlError(this.form.get('password'), 'Password');
  }

}
