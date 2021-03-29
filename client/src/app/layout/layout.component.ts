import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ProfileDialogComponent } from '../components/profile-dialog/profile-dialog.component';
import { SettingsDialogComponent } from './components/settings-dialog/settings-dialog.component';

@Component({
  selector: 'tg-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {
  constructor(private dialog: MatDialog) {}

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
