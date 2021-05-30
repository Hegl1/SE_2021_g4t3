import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ProfileDialogComponent } from 'src/app/components/profile-dialog/profile-dialog.component';
import { User } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-game-player-profile',
  templateUrl: './game-player-profile.component.html',
  styleUrls: ['./game-player-profile.component.scss'],
})
export class GamePlayerProfileComponent {
  @Input() user!: User;
  @Input() isHost!: boolean;

  @Input() isReady: boolean | null = null;
  @Input() isCurrent: boolean = false;
  @Input() isPlayersTurn: boolean = false;

  constructor(private dialog: MatDialog) {}

  /**
   * Opens the users profile dialog
   */
  openProfile() {
    this.dialog.open(ProfileDialogComponent, {
      data: {
        user_id: this.user.id,
      },
    });
  }

  get icon() {
    if (this.isPlayersTurn) {
      return {
        key: 'play_arrow',
        color: '#0f0',
        tooltip: null,
      };
    }

    if (this.isReady !== null) {
      if (this.isReady) {
        return {
          key: 'check',
          color: '#0f0',
          tooltip: 'Player is ready',
        };
      } else {
        return {
          key: 'remove',
          color: '#f12',
          tooltip: 'Player is not ready',
        };
      }
    }

    return {
      key: 'account_circle',
      color: 'inherit',
      tooltip: null,
    };
  }
}
