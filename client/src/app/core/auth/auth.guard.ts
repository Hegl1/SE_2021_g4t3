import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Role } from '../api/ApiInterfaces';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private user: UserService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const redirectUrl = state.url;

    const url = redirectUrl.split('?')[0];

    if (url.startsWith('/admin')) {
      if (url === '/admin/users') {
        if (this.user.hasRole(Role.Admin)) return true;
      } else {
        if (this.user.hasRole(Role.Gamemanager)) return true;
      }
    } else if (this.user.isLoggedin) {
      return true;
    }

    this.router.navigateByUrl(
      this.router.createUrlTree(['/login'], {
        queryParams: {
          redirectUrl,
        },
      })
    );

    return false;
  }
}
