import { AbstractControl } from '@angular/forms';

export const markAllTouched = (control: AbstractControl | null): void => {
  if (!control) {
    return;
  }

  control.markAllAsTouched();
};

export const getControlError = (control: AbstractControl | null, label: string): string | null => {
  if (!control || !control.touched || !control.errors) {
    return null;
  }

  if (control.errors['required']) {
    return `${label} is required`;
  }

  if (control.errors['email']) {
    return 'Enter a valid email address';
  }

  if (control.errors['minlength']) {
    const requiredLength = control.errors['minlength'].requiredLength;
    return `${label} must be at least ${requiredLength} characters`;
  }

  return 'Invalid value';
};
