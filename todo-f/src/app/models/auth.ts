export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface UserInfo {
  username: string;
  roles: string[];
}

export interface RegisterRequest {
  username: string;
  password: string;
}
