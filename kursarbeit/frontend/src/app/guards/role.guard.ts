import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {map, take} from 'rxjs/operators';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.user$.pipe(
      take(1),
      map(user => {
        if (!user) {
          router.navigate(['/']);
          return false;
        }
        const userRole = user.roleName?.toUpperCase() || '';
        if (allowedRoles.some(role => role.toUpperCase() === userRole)) {
          return true;
        }
        router.navigate(['/']);
        return false;
      })
    );
  };
};
