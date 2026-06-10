import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  QueryList,
  ViewChildren,
  inject
} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, finalize, switchMap, takeWhile, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { getControlError, markAllTouched } from '../../../../shared/utils/form-helpers';
import { mapHttpError } from '../../../../shared/utils/error-mapper';
import { matchFields } from '../../../../shared/utils/validators';
import { MessageResponse } from '../../../../shared/models/auth.models';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResetPasswordComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  @ViewChildren('otpInput') private readonly otpInputs!: QueryList<ElementRef<HTMLInputElement>>;

  readonly form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email]],
      code1: ['', [Validators.required, Validators.maxLength(1)]],
      code2: ['', [Validators.required, Validators.maxLength(1)]],
      code3: ['', [Validators.required, Validators.maxLength(1)]],
      code4: ['', [Validators.required, Validators.maxLength(1)]],
      code5: ['', [Validators.required, Validators.maxLength(1)]],
      code6: ['', [Validators.required, Validators.maxLength(1)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: matchFields('newPassword', 'confirmPassword') }
  );

  loading = false;
  resending = false;
  cooldownSeconds = 0;
  showNewPassword = false;
  showConfirmPassword = false;

  ngOnInit(): void {
    const email = this.route.snapshot.queryParamMap.get('email');
    if (email) {
      this.form.patchValue({ email });
    }
    this.startCooldown();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      markAllTouched(this.form);
      return;
    }

    const code = this.getCode();
    if (!code) {
      this.toast.error('Enter the 6-digit verification code.');
      return;
    }

    this.loading = true;

    this.authService
      .resetPassword({
        email: this.form.get('email')?.value ?? '',
        code,
        newPassword: this.form.get('newPassword')?.value ?? ''
      })
      .pipe(
        finalize(() => (this.loading = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response: MessageResponse) => {
          this.toast.success(response.message);
          window.setTimeout(() => this.router.navigate(['/auth/login']), 800);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to reset password.'));
        }
      });
  }

  onResend(): void {
    const emailControl = this.form.get('email');
    if (!emailControl || emailControl.invalid) {
      markAllTouched(this.form);
      this.toast.error('A valid email is required to resend the code.');
      return;
    }

    this.resending = true;

    this.authService
      .resendResetCode(emailControl.value)
      .pipe(
        finalize(() => (this.resending = false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response: MessageResponse) => {
          this.toast.success(response.message);
          this.startCooldown();
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to resend the code.'));
        }
      });
  }

  private startCooldown(): void {
    this.cooldownSeconds = 60;
    this.cdr.markForCheck();

    interval(1000)
      .pipe(
        takeWhile(() => this.cooldownSeconds > 0),
        tap(() => {
          this.cooldownSeconds--;
          this.cdr.markForCheck();
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }

  onOtpInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '');
    input.value = value;
    this.form.get(this.getControlName(index))?.setValue(value, { emitEvent: false });

    if (value && index < this.otpInputs.length - 1) {
      this.focusInput(index + 1);
    }
  }

  onOtpKeydown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace') {
      const control = this.form.get(this.getControlName(index));
      if (!control?.value && index > 0) {
        this.focusInput(index - 1);
      }
      return;
    }

    if (event.key === 'ArrowLeft' && index > 0) {
      this.focusInput(index - 1);
      event.preventDefault();
      return;
    }

    if (event.key === 'ArrowRight' && index < this.otpInputs.length - 1) {
      this.focusInput(index + 1);
      event.preventDefault();
    }
  }

  onOtpPaste(event: ClipboardEvent): void {
    const text = event.clipboardData?.getData('text') ?? '';
    const digits = text.replace(/\D/g, '').slice(0, 6).split('');
    if (digits.length === 0) {
      return;
    }

    event.preventDefault();

    digits.forEach((digit, index) => {
      const control = this.form.get(this.getControlName(index));
      control?.setValue(digit, { emitEvent: false });
      const input = this.otpInputs.get(index)?.nativeElement;
      if (input) {
        input.value = digit;
      }
    });

    const nextIndex = Math.min(digits.length, this.otpInputs.length - 1);
    this.focusInput(nextIndex);
  }

  get emailError(): string | null {
    return getControlError(this.form.get('email'), 'Email');
  }

  get newPasswordError(): string | null {
    return getControlError(this.form.get('newPassword'), 'Password');
  }

  get confirmPasswordError(): string | null {
    const control = this.form.get('confirmPassword');
    if (!control || !control.touched || !control.errors) {
      return null;
    }

    if (control.errors['required']) {
      return 'Please confirm your password';
    }

    if (control.errors['mismatch']) {
      return 'Passwords do not match';
    }

    return null;
  }

  get codeError(): string | null {
    if (this.getCode()) {
      return null;
    }

    const controls = ['code1', 'code2', 'code3', 'code4', 'code5', 'code6'];
    const touched = controls.some((key) => this.form.get(key)?.touched);
    return touched ? 'Enter the 6-digit verification code.' : null;
  }

  get canResend(): boolean {
    return !this.resending && this.cooldownSeconds === 0;
  }

  private getCode(): string {
    const values = [
      this.form.get('code1')?.value,
      this.form.get('code2')?.value,
      this.form.get('code3')?.value,
      this.form.get('code4')?.value,
      this.form.get('code5')?.value,
      this.form.get('code6')?.value
    ];

    if (values.some((value) => !value)) {
      return '';
    }

    return values.join('');
  }

  private getControlName(index: number): string {
    return `code${index + 1}`;
  }

  private focusInput(index: number): void {
    const input = this.otpInputs.get(index)?.nativeElement;
    input?.focus();
    input?.select();
  }
}
