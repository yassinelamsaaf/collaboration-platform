import { HttpErrorResponse } from '@angular/common/http';

export const mapHttpError = (error: unknown, fallback = 'Something went wrong. Please try again.'): string => {
  if (error instanceof HttpErrorResponse) {
    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      return error.error;
    }

    const fieldErrors = error.error?.fields;
    if (fieldErrors && typeof fieldErrors === 'object') {
      const firstError = Object.values(fieldErrors).find((value) => typeof value === 'string');
      if (typeof firstError === 'string' && firstError.trim().length > 0) {
        return firstError;
      }
    }

    const message = error.error?.message || error.error?.error || error.message;
    if (typeof message === 'string' && message.trim().length > 0) {
      return message;
    }
  }

  return fallback;
};
