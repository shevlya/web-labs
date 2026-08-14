import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import {catchError, map, switchMap, tap} from 'rxjs/operators';

import {environment} from '../environment';
import {User} from '../models/user.models';

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
}

interface UserProfileResponse {
  idUser: number;
  userStatus: string;
  idAvatar: number | null;
  avatarUrl: string | null;
  idRole: number;
  roleName: string;
  fio: string;
  email: string;
  birthDate: string | null;
  hasDisability: boolean;
}

@Injectable({providedIn: 'root'})
export class AuthService {
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  private readonly isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  private readonly userSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());

  readonly isLoggedIn$ = this.isLoggedInSubject.asObservable();
  readonly user$ = this.userSubject.asObservable();

  constructor(private readonly http: HttpClient) {
  }

  initialize(): void {
    if (this.hasToken()) {
      this.refreshUserFromServer().subscribe({
        error: () => this.logout()
      });
    }
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): User | null {
    return this.userSubject.getValue();
  }

  register(data: { fio: string; email: string; password: string; hasDisability: boolean }): Observable<LoginResponse> {
    const payload = {
      userStatus: 'ACTIVE',
      idRole: 3,
      idAvatar: null,
      fio: data.fio,
      email: data.email,
      password: data.password,
      birthDate: null,
      hasDisability: data.hasDisability
    };

    return this.http.post<UserProfileResponse>(`${environment.authUrl}/users/register`, payload).pipe(
      switchMap(() => this.login({email: data.email, password: data.password})),
      catchError(err => throwError(() => err))
    );
  }

  login(credentials: { email: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.authUrl}/auth/login`, credentials).pipe(
      tap(response => this.handleAuthSuccess(response)),
      switchMap(response => this.refreshUserFromServer().pipe(map(() => response))),
      catchError(err => {
        this.logout();
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.REFRESH_KEY);
    sessionStorage.removeItem(this.USER_KEY);

    this.isLoggedInSubject.next(false);
    this.userSubject.next(null);
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = sessionStorage.getItem(this.REFRESH_KEY);
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token'));
    }

    return this.http.post<LoginResponse>(`${environment.authUrl}/auth/refresh`, {refreshToken}).pipe(
      tap(response => this.handleAuthSuccess(response)),
      switchMap(response => this.refreshUserFromServer().pipe(map(() => response))),
      catchError(err => {
        this.logout();
        return throwError(() => err);
      })
    );
  }

  refreshUserFromServer(): Observable<User> {
    return this.http.get<UserProfileResponse>(`${environment.apiUrl}/users/me`).pipe(
      tap(profile => this.applyServerProfile(profile)),
      map(profile => this.mapToUser(profile))
    );
  }

  updateProfile(updateData: Partial<{
    fio: string;
    email: string;
    idAvatar: number | null;
    hasDisability: boolean;
    birthDate: string | null
  }>): Observable<User> {
    const current = this.getCurrentUser();

    const payload = {
      fio: current?.fio ?? '',
      email: current?.email ?? '',
      hasDisability: current?.hasDisability ?? false,
      birthDate: current?.birthDate ?? null,
      idAvatar: current?.avatarId ?? null,
      ...updateData
    };

    return this.http.put<UserProfileResponse>(`${environment.apiUrl}/users/me`, payload).pipe(
      tap(profile => this.applyServerProfile(profile)),
      map(profile => this.mapToUser(profile))
    );
  }

  updateDisability(hasDisability: boolean): Observable<User> {
    return this.http.patch<UserProfileResponse>(`${environment.apiUrl}/users/me/disability`, {hasDisability}).pipe(
      tap(profile => this.applyServerProfile(profile)),
      map(profile => this.mapToUser(profile))
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    const payload = {currentPassword, newPassword};
    return this.http.patch<void>(`${environment.apiUrl}/users/me/password`, payload);
  }

  private hasToken(): boolean {
    return !!sessionStorage.getItem(this.TOKEN_KEY);
  }

  private handleAuthSuccess(response: LoginResponse): void {
    sessionStorage.setItem(this.TOKEN_KEY, response.accessToken);
    sessionStorage.setItem(this.REFRESH_KEY, response.refreshToken);

    const partialUser: User = {
      userId: response.userId,
      email: response.email,
      fio: '',
      roleName: '',
      avatarUrl: null,
      avatarId: null,
      hasDisability: false,
      birthDate: null
    };

    sessionStorage.setItem(this.USER_KEY, JSON.stringify(partialUser));
    this.isLoggedInSubject.next(true);
    this.userSubject.next(partialUser);
  }

  private applyServerProfile(profile: UserProfileResponse): void {
    const user = this.mapToUser(profile);
    sessionStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.userSubject.next(user);
  }

  private mapToUser(profile: UserProfileResponse): User {
    return {
      userId: profile.idUser,
      fio: profile.fio,
      email: profile.email,
      roleName: profile.roleName,
      avatarUrl: profile.avatarUrl,
      avatarId: profile.idAvatar,
      hasDisability: profile.hasDisability,
      birthDate: profile.birthDate
    };
  }

  private getUserFromStorage(): User | null {
    const raw = sessionStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
