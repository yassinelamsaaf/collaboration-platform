import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { getControlError, markAllTouched } from '../../../../shared/utils/form-helpers';
import { mapHttpError } from '../../../../shared/utils/error-mapper';
import { matchFields } from '../../../../shared/utils/validators';
import { MessageResponse } from '../../../../shared/models/auth.models';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: [matchFields('password', 'confirmPassword')] }
  );

  loading = false;
  showPassword = false;
  showConfirmPassword = false;

  onSubmit(): void {
    if (this.form.invalid) {
      markAllTouched(this.form);
      return;
    }

    this.loading = true;

    const payload = this.form.getRawValue();

    this.authService
      .register({ username: payload.username, email: payload.email, password: payload.password })
      .pipe(
        finalize(() => (this.loading = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response: MessageResponse) => {
            this.toast.success(response.message);
          window.setTimeout(() => {
            this.router.navigate(['/auth/verify-email'], { queryParams: { email: payload.email } });
          }, 900);
        },
        error: (error: unknown) => {
            this.toast.error(mapHttpError(error, 'Unable to register. Please try again.'));
        }
      });
  }

  get usernameError(): string | null {
    return getControlError(this.form.get('username'), 'Username');
  }

  get emailError(): string | null {
    return getControlError(this.form.get('email'), 'Email');
  }

  get passwordError(): string | null {
    return getControlError(this.form.get('password'), 'Password');
  }

  get confirmPasswordError(): string | null {
    const confirmControl = this.form.get('confirmPassword');
    if (confirmControl?.touched && confirmControl.errors?.['mismatch']) {
      return 'Passwords do not match';
    }

    return getControlError(confirmControl, 'Confirm password');
  }
}
