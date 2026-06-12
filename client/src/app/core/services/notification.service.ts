import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { NotificationResponse, NotificationPrefsResponse, UpdateNotificationPrefsRequest, UnreadCountResponse, PaginatedResponse } from '../../shared/models/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private readonly api: ApiService) {}

  list(params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<NotificationResponse>> {
    return this.api.get<PaginatedResponse<NotificationResponse>>('/notifications', params);
  }

  markAsRead(id: string): Observable<void> {
    return this.api.patch<void>(`/notifications/${id}/read`, null).pipe(
      tap(() => this.refreshUnreadCount())
    );
  }

  markAllAsRead(): Observable<void> {
    return this.api.patch<void>('/notifications/read-all', null).pipe(
      tap(() => this.unreadCountSubject.next(0))
    );
  }

  getUnreadCount(): Observable<UnreadCountResponse> {
    return this.api.get<UnreadCountResponse>('/notifications/unread-count').pipe(
      tap((res) => this.unreadCountSubject.next(res.count))
    );
  }

  refreshUnreadCount(): void {
    this.getUnreadCount().subscribe();
  }

  getPrefs(): Observable<NotificationPrefsResponse> {
    return this.api.get<NotificationPrefsResponse>('/notifications/prefs');
  }

  updatePrefs(body: UpdateNotificationPrefsRequest): Observable<NotificationPrefsResponse> {
    return this.api.put<NotificationPrefsResponse>('/notifications/prefs', body);
  }
}
