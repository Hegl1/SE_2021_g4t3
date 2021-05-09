import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { User, UserStats } from 'src/app/core/api/ApiInterfaces';
import { PlayerListDialogComponent } from '../player-list-dialog/player-list-dialog.component';

@Component({
  selector: 'tg-profile-dialog',
  templateUrl: './profile-dialog.component.html',
  styleUrls: ['./profile-dialog.component.scss'],
})
export class ProfileDialogComponent implements OnInit {
  loading = false;

  user: User | null = null;
  userStats: UserStats | null = null;

  wonGames: number = 0;
  lostGames: number = 0;

  constructor(
    private api: ApiService,
    @Inject(MAT_DIALOG_DATA)
    public data: { user_id: number },
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ProfileDialogComponent>,
    private dialog: MatDialog
  ) {}

  async ngOnInit() {
    this.loading = true;
    this.user = this.userStats = null;

    let res = await Promise.all([this.api.getUser(this.data.user_id), this.api.getUserStats(this.data.user_id)]);

    if (!res[0].isOK() || !res[1].isOK()) {
      this.snackBar.open('Error loading user-information!', 'OK', {
        panelClass: 'action-warn',
        duration: 10000,
      });
      this.dialogRef.close();
      return;
    }

    this.loading = false;

    this.user = res[0].value;
    this.userStats = res[1].value;

    this.wonGames = this.lostGames = 0;

    this.userStats?.won_games.forEach((element) => (this.wonGames += element.amount));
    this.userStats?.lost_games.forEach((element) => (this.lostGames += element.amount));
  }

  /**
   * Returns a list of users, the player has played with.
   * If there are more than <i>amount</i> users, truncate is set to true.
   *
   * @param amount the amount of users to contain
   * @returns the players and if they should be truncated
   */
  getPlayedWith(amount = 5) {
    return {
      players: this.userStats?.played_with,
      truncate: (this.userStats?.played_with.length || 0) > amount,
    };
  }

  showPlayedWithList() {
    this.dialog.open(PlayerListDialogComponent, {
      data: {
        users: this.userStats?.played_with || [],
      },
    });
  }
}
