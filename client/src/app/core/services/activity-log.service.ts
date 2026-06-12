import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ActivityLogResponse } from '../../shared/models/activity-log.models';
import { PaginatedResponse } from '../../shared/models/project.models';

@Injectable({ providedIn: 'root' })
export class ActivityLogService {
  constructor(private readonly api: ApiService) {}

  list(projectRef: string, params?: Record<string, string | number | boolean | undefined>): Observable<PaginatedResponse<ActivityLogResponse>> {
    return this.api.get<PaginatedResponse<ActivityLogResponse>>(`/projects/${projectRef}/activity-log`, params);
  }
}
