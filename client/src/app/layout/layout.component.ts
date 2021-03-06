import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSidenav } from '@angular/material/sidenav';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { ProfileDialogComponent } from '../components/profile-dialog/profile-dialog.component';
import { ApiService } from '../core/api/api.service';
import { UserService } from '../core/auth/user.service';
import { GameService } from '../core/game/game.service';
import ThemeService, { ThemeType } from '../core/theme/theme.service';
import { SettingsDialogComponent } from './components/settings-dialog/settings-dialog.component';

const maxWidthPx = 1000;

@Component({
  selector: 'tg-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {
  inGame = false;
  isSmallScreen = this.breakpointObserver.isMatched(`(max-width: ${maxWidthPx}px)`);

  @ViewChild('drawer') public sidenav!: MatSidenav;

  constructor(
    private dialog: MatDialog,
    private breakpointObserver: BreakpointObserver,
    private user: UserService,
    private router: Router,
    private api: ApiService,
    private game: GameService,
    private theme: ThemeService
  ) {
    this.breakpointObserver
      .observe([`(max-width: ${maxWidthPx}px)`])
      .subscribe((result) => (this.isSmallScreen = result.matches));

    this.router.events.subscribe(async (val) => {
      if (val instanceof NavigationStart && this.isSmallScreen && this.sidenav) {
        this.sidenav.close();
      }
      if (val instanceof NavigationEnd) {
        let ingame = await this.api.getIngame();

        if (ingame.isOK() && ingame.value) {
          this.inGame = ingame.value;
        } else {
          this.inGame = false;

          if (this.game.connected) {
            this.game.reset(false);
          }
        }
      }
    });
  }

  openSettings() {
    this.dialog.open(SettingsDialogComponent);
  }

  openProfile() {
    this.dialog.open(ProfileDialogComponent, {
      data: {
        user_id: this.user.user?.id,
      },
    });
  }

  /**
   * Checks whether the current user has permissions for a given role
   *
   * @param role the searched role in string form
   * @returns true, if the user has the permissions for the role
   */
  userHasRole(role: 'admin' | 'gamemanager' | 'player') {
    let parsedRole = UserService.parseRole(role);

    if (parsedRole == null) {
      return false;
    }

    return this.user.hasRole(parsedRole);
  }

  get username() {
    return this.user.user?.username;
  }

  get currentTheme() {
    return this.theme.currentTheme;
  }

  setTheme(theme: string) {
    this.theme.setTheme(theme);
  }
}
