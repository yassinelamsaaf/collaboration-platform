import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.constants';

type RequestParams = Record<string, string | number | boolean | undefined>;

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private readonly http: HttpClient) {}

  get<T>(path: string, params?: RequestParams): Observable<T> {
    return this.http.get<T>(this.buildUrl(path), { params: this.buildParams(params) });
  }

  post<T>(path: string, body: unknown, params?: RequestParams): Observable<T> {
    return this.http.post<T>(this.buildUrl(path), body, { params: this.buildParams(params) });
  }

  patch<T>(path: string, body: unknown, params?: RequestParams): Observable<T> {
    return this.http.patch<T>(this.buildUrl(path), body, { params: this.buildParams(params) });
  }

  put<T>(path: string, body: unknown, params?: RequestParams): Observable<T> {
    return this.http.put<T>(this.buildUrl(path), body, { params: this.buildParams(params) });
  }

  delete<T>(path: string, params?: RequestParams): Observable<T> {
    return this.http.delete<T>(this.buildUrl(path), { params: this.buildParams(params) });
  }

  private buildUrl(path: string): string {
    if (path.startsWith('http')) {
      return path;
    }
    const normalizedPath = path.startsWith('/') ? path.slice(1) : path;
    return `${API_BASE_URL}/${normalizedPath}`;
  }

  private buildParams(params?: RequestParams): HttpParams | undefined {
    if (!params) {
      return undefined;
    }

    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        httpParams = httpParams.set(key, String(value));
      }
    });

    return httpParams;
  }
}
