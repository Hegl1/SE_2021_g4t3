import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Role, User } from '../api/ApiInterfaces';
import StorageNames from '../StorageNames';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private _user: User | null = null;
  private _token: string | null = null;

  constructor(private router: Router) {
    this.loadToken();
  }

  /**
   * Stores the user and token
   *
   * @param user the user to store
   * @param token the token of the user
   * @param save whether to store the token
   */
  login(token: string, save: boolean = false) {
    this.remove();

    if (save) {
      localStorage.setItem(StorageNames.Token, token);
    } else {
      document.cookie = StorageNames.Token + '=' + token;
    }

    this.loadToken();
  }

  /**
   * Loads the token and user from storage or cookies (if stored before)
   *
   * @returns the loaded token or null, if no token was found
   */
  private loadToken(): void {
    let token = null;

    if (this.isRemembered) {
      token = localStorage.getItem(StorageNames.Token);
    } else {
      let token_cookie = document.cookie
        .split('; ')
        .map((el) => el.split('='))
        .find((el) => el[0] == StorageNames.Token);

      if (token_cookie != null) {
        token = token_cookie[1];
      }
    }

    if (token) {
      try {
        this._user = JSON.parse(atob(token.split('.')[1])).user;
        this._token = token;
      } catch (e) {
        if (this.isLoggedin) {
          this.logoutReason('badToken');
        } else {
          this.remove();
        }
      }
    }
  }

  /**
   * Removes the stored user and token from storage and cookies
   */
  private remove() {
    localStorage.removeItem(StorageNames.Token);

    document.cookie = StorageNames.Token + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC';
  }

  /**
   * The stored user
   */
  get user() {
    return this._user;
  }

  /**
   * The stored token
   */
  get token() {
    return this._token;
  }

  /**
   * Whether the token was stored (not in cookies)
   */
  get isRemembered() {
    return localStorage.getItem(StorageNames.Token) !== null;
  }

  /**
   * Whether the user is loggedin
   */
  get isLoggedin() {
    return this.token != null;
  }

  /**
   * Checks whether the current user has permissions for a given role
   *
   * @param role the searched role
   * @returns true, if the user has the permissions for the role
   */
  hasRole(role: Role) {
    if (this.user === null) return false;

    switch (this.user.role) {
      case Role.Admin:
        return true;
      case Role.Gamemanager:
        return role != Role.Admin;
      case Role.Player:
        return role == Role.Player;
    }
  }

  /**
   * Logsout the user and removes him from storage and cookies
   *
   * @param redirect whether to redirect the user to the login page afterwards
   */
  async logout(redirect: boolean = true) {
    this.remove();

    if (redirect) this.router.navigateByUrl('/login');
  }

  /**
   * Logsout the user, removes him from storage and cookies and redirects him to the login page with a set reason
   *
   * @param redirect whether to redirect the user to the login page afterwards
   */
  async logoutReason(reason: string) {
    this.logout(false);

    this.router.navigateByUrl(`/login?reason=${reason}`);
  }
}
