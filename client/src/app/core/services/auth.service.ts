import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, of, tap, throwError } from 'rxjs';

import { ApiService } from './api.service';
import {
  AuthResponse,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  UserProfile,
  VerifyCodeRequest
} from '@shared/models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authenticatedSubject = new BehaviorSubject<boolean>(false);
  readonly isAuthenticated$ = this.authenticatedSubject.asObservable();

  private readonly profileSubject = new BehaviorSubject<UserProfile | null>(null);
  readonly profile$ = this.profileSubject.asObservable();

  constructor(private readonly api: ApiService) {}

  forgotPassword(payload: { email: string }): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/forgot-password', payload);
  }

  resetPassword(payload: { email: string; code: string; newPassword: string }): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/reset-password', payload);
  }

  resendResetCode(email: string): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/resend-reset-code', null, { email });
  }

  register(payload: RegisterRequest): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/register', payload);
  }

  verifyCode(payload: VerifyCodeRequest): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/verify-code', payload);
  }

  resendCode(email: string): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/resend-code', null, { email });
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('auth/login', payload).pipe(
      tap((profile) => {
        this.authenticatedSubject.next(true);
        this.profileSubject.next(profile);
      })
    );
  }

  logout(): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/logout', null).pipe(
      tap(() => {
        this.authenticatedSubject.next(false);
        this.profileSubject.next(null);
      })
    );
  }

  checkSession(): Observable<MessageResponse> {
    return this.api.post<MessageResponse>('auth/refresh', null).pipe(
      tap(() => this.authenticatedSubject.next(true)),
      catchError((error) => {
        this.authenticatedSubject.next(false);
        return throwError(() => error);
      })
    );
  }

  getProfile(): Observable<UserProfile | null> {
    return this.api.get<UserProfile>('auth/me').pipe(
      tap((profile) => this.profileSubject.next(profile)),
      catchError(() => {
        this.profileSubject.next(null);
        return of(null);
      })
    );
  }
}
