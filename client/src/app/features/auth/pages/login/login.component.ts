import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { getControlError, markAllTouched } from '../../../../shared/utils/form-helpers';
import { mapHttpError } from '../../../../shared/utils/error-mapper';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    rememberMe: [false]
  });
  loading = false;

  onSubmit(): void {
    if (this.form.invalid) {
      markAllTouched(this.form);
      return;
    }

    this.loading = true;

    const { email, password } = this.form.getRawValue();

    this.authService
      .login({ email, password })
      .pipe(
        finalize(() => (this.loading = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
            this.toast.success('Signed in successfully. Redirecting...');
            window.setTimeout(() => this.router.navigate(['/']), 700);
        },
        error: (error: unknown) => {
            this.toast.error(mapHttpError(error, 'Unable to sign in. Please try again.'));
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
