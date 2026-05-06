import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AuthService } from '../../../core/auth/auth.service';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { getControlError, markAllTouched } from '../../../shared/helpers/form-helpers';
import { mapHttpError } from '../../../shared/helpers/error-mapper';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgIf, AlertComponent],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerifyEmailComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    code: ['', [Validators.required, Validators.minLength(6)]]
  });

  loading = false;
  resending = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    const email = this.route.snapshot.queryParamMap.get('email');
    if (email) {
      this.form.patchValue({ email });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      markAllTouched(this.form);
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService
      .verifyCode(this.form.getRawValue())
      .pipe(
        finalize(() => (this.loading = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response) => {
          this.successMessage = response.message;
          window.setTimeout(() => this.router.navigate(['/auth/login']), 800);
        },
        error: (error) => {
          this.errorMessage = mapHttpError(error, 'Verification failed. Please try again.');
        }
      });
  }

  onResend(): void {
    const emailControl = this.form.get('email');
    if (!emailControl || emailControl.invalid) {
      markAllTouched(this.form);
      this.errorMessage = 'Enter your email to resend the verification code.';
      return;
    }

    this.resending = true;
    this.errorMessage = '';

    this.authService
      .resendCode(emailControl.value)
      .pipe(
        finalize(() => (this.resending = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response) => {
          this.successMessage = response.message;
        },
        error: (error) => {
          this.errorMessage = mapHttpError(error, 'Unable to resend the code.');
        }
      });
  }

  get emailError(): string | null {
    return getControlError(this.form.get('email'), 'Email');
  }

  get codeError(): string | null {
    return getControlError(this.form.get('code'), 'Verification code');
  }
}
