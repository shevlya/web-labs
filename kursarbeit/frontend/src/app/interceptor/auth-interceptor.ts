import {HttpInterceptorFn, HttpRequest, HttpHandlerFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, switchMap, throwError} from 'rxjs';
import {AuthService} from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);

  if (req.url.includes('/auth/refresh') || req.url.includes('/auth/login') || req.url.includes('/users/register')) {
    return next(req);
  }

  const token = authService.getToken();

  const reqWithToken = token ? req.clone({setHeaders: {Authorization: `Bearer ${token}`}}) : req;

  return next(reqWithToken).pipe(
    catchError(error => {
      if (error.status === 401) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            const newToken = authService.getToken();
            const retryReq = newToken
              ? req.clone({setHeaders: {Authorization: `Bearer ${newToken}`}})
              : req;
            return next(retryReq);
          }),
          catchError(refreshError => {
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
