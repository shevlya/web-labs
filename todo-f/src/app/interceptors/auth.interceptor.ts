import {HttpInterceptorFn} from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const credentials = sessionStorage.getItem('access_token');
  if (credentials) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${credentials}`)
    });
    return next(authReq);
  }
  return next(req);
};
