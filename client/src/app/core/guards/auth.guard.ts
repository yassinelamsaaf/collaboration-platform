import { inject } from '@angular/core';
import { Router, type ActivatedRouteSnapshot, type CanActivateFn, type RouterStateSnapshot } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '@core/services/auth.service';

export const authGuard: CanActivateFn = (_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.checkSession().pipe(
    map(() => true),
    catchError(() => {
      router.navigate(['/auth/login']);
      return of(false);
    })
  );
};
