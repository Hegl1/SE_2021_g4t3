import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { User } from 'src/app/core/api/ApiInterfaces';
import { ProfileDialogComponent } from '../profile-dialog/profile-dialog.component';

@Component({
  selector: 'tg-player-name',
  templateUrl: './player-name.component.html',
  styleUrls: ['./player-name.component.scss'],
})
export class PlayerNameComponent {
  @Input() player!: User;

  constructor(private dialog: MatDialog) {}

  /**
   * Opens the users profile dialog
   */
  openProfile() {
    this.dialog.closeAll();

    this.dialog.open(ProfileDialogComponent, {
      data: {
        user_id: this.player.id,
      },
    });
  }
}
