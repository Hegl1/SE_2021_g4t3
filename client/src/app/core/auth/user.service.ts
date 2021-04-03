import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Role, User } from '../api/ApiInterfaces';
import StorageNames from '../StorageNames';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private router: Router) {}

  /**
   * Stores the user and token
   *
   * @param user the user to store
   * @param token the token of the user
   * @param save whether to store the token
   */
  login(user: User, token: string, save: boolean = false) {
    this.remove();

    if (save) {
      localStorage.setItem(StorageNames.User, JSON.stringify(user));
      localStorage.setItem(StorageNames.Token, token);
    } else {
      document.cookie = StorageNames.User + '=' + JSON.stringify(user);
      document.cookie = StorageNames.Token + '=' + token;
    }
  }

  /**
   * Loads the user from storage or cookies (if stored before)
   *
   * @returns the loaded user or null, if no user was found
   */
  private loadUser(): User | null {
    let user = null;

    if (this.isRemembered) {
      user = localStorage.getItem(StorageNames.User);
    } else {
      let user_cookie = document.cookie
        .split('; ')
        .map((el) => el.split('='))
        .find((el) => el[0] == StorageNames.User);

      if (user_cookie != null) {
        user = user_cookie[1];
      }
    }

    if (user === null) {
      this.remove();
      return null;
    }

    try {
      return JSON.parse(user);
    } catch (e) {
      this.remove();
      return null;
    }
  }

  /**
   * Loads the token from storage or cookies (if stored before)
   *
   * @returns the loaded token or null, if no token was found
   */
  private loadToken(): string | null {
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

    return token;
  }

  /**
   * Removes the stored user and token from storage and cookies
   */
  private remove() {
    localStorage.removeItem(StorageNames.User);
    localStorage.removeItem(StorageNames.Token);

    document.cookie = StorageNames.User + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC';
    document.cookie = StorageNames.Token + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC';
  }

  /**
   * The stored user
   */
  get user() {
    return this.loadUser();
  }

  /**
   * The stored token
   */
  get token() {
    return this.loadToken();
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
    return this.loadToken() != null;
  }

  /**
   * Checks whether the current user has permissions for a given role
   *
   * @param role the searched role
   * @returns true, if the user has the permissions for the role
   */
  hasRole(role: Role) {
    let user = this.user;

    if (user === null) return false;

    switch (user.role) {
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
}
