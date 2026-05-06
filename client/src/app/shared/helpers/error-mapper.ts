import { HttpErrorResponse } from '@angular/common/http';

export const mapHttpError = (error: unknown, fallback = 'Something went wrong. Please try again.'): string => {
  if (error instanceof HttpErrorResponse) {
    const message = error.error?.message || error.error?.error || error.message;
    if (typeof message === 'string' && message.trim().length > 0) {
      return message;
    }
  }

  return fallback;
};
