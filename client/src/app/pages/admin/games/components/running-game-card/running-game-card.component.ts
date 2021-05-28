import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { Role, RunningGame } from 'src/app/core/api/ApiInterfaces';
import { UserService } from 'src/app/core/auth/user.service';
import { GameCodePipe } from 'src/app/core/pipes/gamecode.pipe';
import { RunningGameDetailsDialogComponent } from '../running-game-details-dialog/running-game-details-dialog.component';

@Component({
  selector: 'tg-running-game-card',
  templateUrl: './running-game-card.component.html',
  styleUrls: ['./running-game-card.component.scss'],
})
export class GameCardComponent {
  @Input()
  set runningGame(runningGame: RunningGame | null) {
    this._runningGame = runningGame;

    this.players = runningGame?.teams.reduce((acc, team) => acc + team.players.length, 0) || 0;
  }

  @Input() loading = false;

  @Output() closeGame = new EventEmitter<void>();

  private _runningGame: RunningGame | null = null;
  players = 0;

  constructor(private dialog: MatDialog, private user: UserService) {}

  get runningGame() {
    return this._runningGame;
  }

  get isUserAdmin() {
    return this.user.user?.role === Role.Admin;
  }

  /**
   * Opens a confirm dialog and if it is confirmed,
   * the parent is notified that this game should be deleted
   */
  async doCloseGame() {
    if (!this.runningGame) return;

    let code = GameCodePipe.prototype.transform(this.runningGame.code);

    let res = await this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Confirmation',
          content: `Are you sure you want to close the game with code (${code})?`,
          warn: true,
        },
      })
      .afterClosed()
      .toPromise();

    if (res) {
      this.closeGame.emit();
    }
  }

  /**
   * Opens a dialog to show the current details of the
   * running game
   */
  showDetails() {
    this.dialog.open(RunningGameDetailsDialogComponent, {
      data: {
        runningGame: this.runningGame,
      },
    });
  }
}
