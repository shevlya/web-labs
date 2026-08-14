import {HttpInterceptorFn} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const credentials = sessionStorage.getItem('access_token');
  // const now = +new Date();
  // if (token['exp'] < now) {
  //   return auth.refreshToken()
  //     .pipe(
  //       switchMap(() => {
  //         const authReq = req.clone({
  //           headers: req.headers.set('Authorization', `Bearer ${credentials}`)
  //         });

  //         return next(authReq);
  //       })
  //     )
  // }

  if (credentials) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${credentials}`)
    });

    return next(authReq);
  }
  return next(req);
};
