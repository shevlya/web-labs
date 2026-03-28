import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {LoginRequest, TokenResponse, UserInfo} from '../models/auth';
import {Observable, tap} from 'rxjs';
import {ERROR_MESSAGES} from '../constants/errors';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly authUrl = `${environment.apiUrl}/auth`;

  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';


  constructor(private http: HttpClient, private router: Router) {
  }

  login(request: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.authUrl}/login`, request).pipe(
      tap(response => {
        sessionStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
        sessionStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!sessionStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getUserId(): number | null {
    const token = this.getAccessToken();
    if (!token) return null;
    try {
      const base64url = token.split('.')[0];
      let base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');

      while (base64.length % 4 !== 0) {
        base64 += '=';
      }

      const binaryString = atob(base64);
      const bytes = new Uint8Array(binaryString.length);
      for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
      }
      const utf8String = new TextDecoder('utf-8').decode(bytes);
      const payload = JSON.parse(utf8String);
      const id = payload.userId ?? payload.id ?? payload.sub;
      return id != null ? Number(id) : null;
    } catch (e) {
      console.error(ERROR_MESSAGES.AUTH.TOKEN_INVALID, e);
      return null;
    }
  }

  /*
  getUserId(): number | null {
    const token = this.getAccessToken();
    if (!token) return null;
    try {
      //jwt base64url замена символов перед atob
      const base64url = token.split('.')[0];
      const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64));
      return payload.userId ?? null;
    } catch {
      return null;
    }
  }
  */

  getMe(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.authUrl}/me`);
  }

  refreshToken(): Observable<TokenResponse> {
    const refreshToken = sessionStorage.getItem(this.REFRESH_TOKEN_KEY);
    return this.http.post<TokenResponse>(`${this.authUrl}/refresh`, {refreshToken}).pipe(
      tap(response => {
        sessionStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
        sessionStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
      })
    )
  }
}
