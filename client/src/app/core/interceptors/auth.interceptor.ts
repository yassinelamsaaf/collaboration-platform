import { Injectable, NgZone } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '@core/services/api.constants';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly zone: NgZone) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (req.url.startsWith(API_BASE_URL)) {
      req = req.clone({ withCredentials: true });
    }

    return new Observable<HttpEvent<unknown>>((observer) => {
      const subscription = next.handle(req).subscribe({
        next: (event) => this.zone.run(() => observer.next(event)),
        error: (error: unknown) => this.zone.run(() => observer.error(error)),
        complete: () => this.zone.run(() => observer.complete())
      });

      return () => subscription.unsubscribe();
    });
  }
}
