import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { markAllTouched } from '../../../../shared/utils/form-helpers';
import { mapHttpError } from '../../../../shared/utils/error-mapper';
import { MessageResponse } from '../../../../shared/models/auth.models';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  loading = false;

  onSubmit(): void {
    if (this.form.invalid) {
      markAllTouched(this.form);
      return;
    }

    this.loading = true;
    const email = this.form.get('email')?.value ?? '';

    this.authService
      .forgotPassword({ email })
      .pipe(
        finalize(() => (this.loading = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response: MessageResponse) => {
          this.toast.success(response.message);
          window.setTimeout(() => this.router.navigate(['/auth/reset-password'], { queryParams: { email } }), 600);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to send reset code.'));
        }
      });
  }

  get emailError(): string | null {
    const control = this.form.get('email');
    if (!control || !control.touched || !control.errors) {
      return null;
    }

    if (control.errors['required']) {
      return 'Email is required';
    }

    if (control.errors['email']) {
      return 'Enter a valid email address';
    }

    return 'Invalid email address';
  }
}
