import { HttpErrorResponse } from '@angular/common/http';

export const mapHttpError = (error: unknown, fallback = 'Something went wrong. Please try again.'): string => {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Unable to reach the server. Check your connection or try again.';
    }

    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      try {
        const parsed = JSON.parse(error.error);
        const parsedMessage = parsed?.message;
        const parsedFields = parsed?.fields;
        if (typeof parsedMessage === 'string' && parsedMessage.trim().length > 0) {
          return parsedMessage;
        }
        if (parsedFields && typeof parsedFields === 'object') {
          const firstParsedField = Object.values(parsedFields).find(
            (value) => typeof value === 'string'
          );
          if (typeof firstParsedField === 'string' && firstParsedField.trim().length > 0) {
            return firstParsedField;
          }
        }
      } catch {
        return error.error;
      }
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

    switch (error.status) {
      case 400:
        return 'Invalid request. Please check your inputs.';
      case 401:
        return 'Invalid credentials or session expired.';
      case 403:
        return 'Access denied. Please verify your account.';
      case 404:
        return 'Requested resource was not found.';
      case 409:
        return 'Email or username already exists.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return fallback;
    }
  }

  return fallback;
};
