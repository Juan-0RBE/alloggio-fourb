import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService, API_URL } from './auth.service';
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  if (!req.url.startsWith(API_URL)) return next(req);

  const isAuthEndpoint = req.url.includes('/auth/');
  const token = auth.getToken();
  let headers = req.headers;
  if (token && !isAuthEndpoint) headers = headers.set('Authorization', `Bearer ${token}`);

  return next(req.clone({ headers })).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthEndpoint) auth.logout();
      return throwError(() => err);
    })
  );
};
