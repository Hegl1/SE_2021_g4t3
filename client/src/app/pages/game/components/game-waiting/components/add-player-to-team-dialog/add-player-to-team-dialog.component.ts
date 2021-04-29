import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';

@Component({
  selector: 'tg-add-player-to-team-dialog',
  templateUrl: './add-player-to-team-dialog.component.html',
  styleUrls: ['./add-player-to-team-dialog.component.scss'],
})
export class AddPlayerToTeamDialogComponent {
  username = '';
  password = '';

  saving = false;

  constructor(
    private api: ApiService,
    @Inject(MAT_DIALOG_DATA)
    public data: { index: number; name: string },
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<AddPlayerToTeamDialogComponent>
  ) {}

  /**
   * Adds the player with the given username/password to the team
   */
  async save() {
    if (this.username === '' || this.password === '') return;

    this.dialogRef.disableClose = true;
    this.saving = true;

    let res = await this.api.addIngamePlayerToTeam(this.data.index, this.username, this.password);

    this.dialogRef.disableClose = false;
    this.saving = false;

    if (res.isOK()) {
      this.dialogRef.close(true);
    } else {
      let message = 'Error adding player to team!';

      if (res.isBadRequest()) {
        message = 'Username or password is wrong!';
      }

      this.snackBar.open(message, 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });
    }
  }
}
