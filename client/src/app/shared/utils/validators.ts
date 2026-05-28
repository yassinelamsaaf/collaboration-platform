import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const matchFields = (field: string, confirmField: string): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    const group = control;
    const fieldControl = group.get(field);
    const confirmControl = group.get(confirmField);

    if (!fieldControl || !confirmControl) {
      return null;
    }

    if (confirmControl.errors && !confirmControl.errors['mismatch']) {
      return null;
    }

    if (fieldControl.value !== confirmControl.value) {
      confirmControl.setErrors({ mismatch: true });
      return { mismatch: true };
    }

    confirmControl.setErrors(null);
    return null;
  };
};
