export interface MessageResponse {
  message: string;
}

export interface AuthResponse {
  id: string;
  email: string;
  role: string;
  username?: string;
}

export interface UserProfile {
  id: string;
  email: string;
  role: string;
  username?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface VerifyCodeRequest {
  email: string;
  code: string;
}
