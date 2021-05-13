import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { RunningGame } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-running-game-details-dialog',
  templateUrl: './running-game-details-dialog.component.html',
  styleUrls: ['./running-game-details-dialog.component.scss'],
})
export class RunningGameDetailsDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      runningGame: RunningGame;
    }
  ) {}
}
