import { BreakpointObserver } from '@angular/cdk/layout';
import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ProfileDialogComponent } from '../components/profile-dialog/profile-dialog.component';
import { SettingsDialogComponent } from './components/settings-dialog/settings-dialog.component';

const maxWidthPx = 1000;

@Component({
  selector: 'tg-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {
  isSmallScreen = this.breakpointObserver.isMatched(`(max-width: ${maxWidthPx}px)`);

  constructor(private dialog: MatDialog, private breakpointObserver: BreakpointObserver) {
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
        user_id: 0, // TODO: set user_id
      },
    });
  }
}
