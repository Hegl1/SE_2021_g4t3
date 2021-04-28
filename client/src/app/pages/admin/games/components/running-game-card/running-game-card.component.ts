import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { RunningGame } from 'src/app/core/api/ApiInterfaces';
import { GameCodePipe } from 'src/app/core/pipes/gamecode.pipe';

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

  constructor(private dialog: MatDialog) {}

  get runningGame() {
    return this._runningGame;
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
}
