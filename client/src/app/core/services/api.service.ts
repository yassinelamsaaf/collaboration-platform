import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL, AUTH_API_BASE_URL } from './api.constants';

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

  put<T>(path: string, body: unknown, params?: RequestParams): Observable<T> {
    return this.http.put<T>(this.buildUrl(path), body, { params: this.buildParams(params) });
  }

  patch<T>(path: string, body: unknown, params?: RequestParams): Observable<T> {
    return this.http.patch<T>(this.buildUrl(path), body, { params: this.buildParams(params) });
  }

  delete<T>(path: string, params?: RequestParams): Observable<T> {
    return this.http.delete<T>(this.buildUrl(path), { params: this.buildParams(params) });
  }

  buildUrl(path: string): string {
    if (path.startsWith('http')) {
      return path;
    }
    if (path.startsWith('/')) {
      return `${API_BASE_URL}${path}`;
    }
    return `${AUTH_API_BASE_URL}/${path}`;
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