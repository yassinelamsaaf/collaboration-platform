type RuntimeEnv = {
  apiBaseUrl?: string;
};

declare global {
  interface Window {
    __env?: RuntimeEnv;
  }
}

function normalizeBaseUrl(value: string | undefined): string {
  return (value ?? 'http://localhost:8080/api').replace(/\/+$/, '');
}

// The Angular bundle reads API config from a tiny runtime file so the same image
// can be reused locally and in Docker without recompiling for each environment.
export const API_BASE_URL = normalizeBaseUrl(window.__env?.apiBaseUrl);
export const AUTH_API_BASE_URL = `${API_BASE_URL}/auth`;
