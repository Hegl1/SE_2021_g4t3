import { BreakpointObserver } from '@angular/cdk/layout';
import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ProfileDialogComponent } from '../components/profile-dialog/profile-dialog.component';
import { Role } from '../core/api/ApiInterfaces';
import { UserService } from '../core/auth/user.service';
import { SettingsDialogComponent } from './components/settings-dialog/settings-dialog.component';

const maxWidthPx = 1000;

@Component({
  selector: 'tg-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {
  isSmallScreen = this.breakpointObserver.isMatched(`(max-width: ${maxWidthPx}px)`);

  constructor(private dialog: MatDialog, private breakpointObserver: BreakpointObserver, private user: UserService) {
    this.breakpointObserver
      .observe([`(max-width: ${maxWidthPx}px)`])
      .subscribe((result) => (this.isSmallScreen = result.matches));
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
}
