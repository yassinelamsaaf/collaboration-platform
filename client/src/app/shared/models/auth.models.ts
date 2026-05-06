export interface MessageResponse {
  message: string;
}

export interface AuthResponse {
  id: string;
  email: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface VerifyCodeRequest {
  email: string;
  code: string;
}
