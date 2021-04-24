import { Component, EventEmitter, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CreateGameDialogComponent } from './components/create-game-dialog/create-game-dialog.component';

@Component({
  selector: 'tg-create-game-card',
  templateUrl: './create-game-card.component.html',
  styleUrls: ['./create-game-card.component.scss'],
})
export class CreateGameCardComponent {
  @Output('joinGameCode') joinGameCode = new EventEmitter<number>();

  constructor(private dialog: MatDialog) {}

  /**
   * Opens a create-game-dialog and if it was created successfully
   * outputs the game code to the parent
   */
  createGame() {
    this.dialog
      .open(CreateGameDialogComponent)
      .afterClosed()
      .subscribe((code) => {
        if (code != null) {
          this.joinGameCode.emit(code);
        }
      });
  }
}
