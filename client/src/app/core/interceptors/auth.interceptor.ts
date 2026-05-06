import { HttpInterceptorFn } from '@angular/common/http';

import { AUTH_API_BASE_URL } from '../api/api.constants';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith(AUTH_API_BASE_URL)) {
    req = req.clone({ withCredentials: true });
  }

  return next(req);
};
