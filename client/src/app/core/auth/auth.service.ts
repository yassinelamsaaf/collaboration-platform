import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';

import { ApiService } from '../api/api.service';
import {
  AuthResponse,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  VerifyCodeRequest
} from '../../shared/models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authenticatedSubject = new BehaviorSubject<boolean>(false);
  readonly isAuthenticated$ = this.authenticatedSubject.asObservable();

  constructor(private readonly api: ApiService) {}

  register(payload: RegisterRequest): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('register', payload);
  }

  verifyCode(payload: VerifyCodeRequest): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('verify-code', payload);
  }

  resendCode(email: string): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('resend-code', null, { email });
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('login', payload).pipe(
      tap(() => this.authenticatedSubject.next(true))
    );
  }

  logout(): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('logout', null).pipe(
      tap(() => this.authenticatedSubject.next(false))
    );
  }

  checkSession(): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('refresh', null).pipe(
      tap(() => this.authenticatedSubject.next(true)),
      catchError((error) => {
        this.authenticatedSubject.next(false);
        return throwError(() => error);
      })
    );
  }
}
