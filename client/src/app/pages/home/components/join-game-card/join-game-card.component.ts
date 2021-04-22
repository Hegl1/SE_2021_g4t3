import { Component, EventEmitter, Output } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';

@Component({
  selector: 'tg-join-game-card',
  templateUrl: './join-game-card.component.html',
  styleUrls: ['./join-game-card.component.scss'],
})
export class JoinGameCardComponent {
  input: string = '';
  code: number | null = null;

  icon: { color: 'primary' | 'accent' | 'warn' | null; value: string } = {
    color: null,
    value: 'vpn_key',
  };

  loading = false;

  @Output('joinGameCode') joinGameCode = new EventEmitter<number>();

  constructor(private api: ApiService, private snackBar: MatSnackBar) {}

  /**
   * Handles the update of the join code input,
   * parses it and inserts '-' every 2 integers
   */
  updateJoinCode() {
    this.code = null;

    let joinCode = this.input.replace(/-/g, '');

    if (joinCode.length > 8) {
      joinCode = joinCode.substr(0, 8);
    }

    let code = parseInt(joinCode) || null;

    if (code === null) {
      this.input = '';
      return;
    }

    joinCode = code.toString();

    let parts: string[] = [];

    for (let i = 0; i < joinCode.length; i += 2) {
      parts.push(joinCode.substr(i, 2));
    }

    this.input = parts.join('-');

    if (joinCode.length === 8) {
      this.code = code;
      this.checkJoinCode();
    }
  }

  /**
   * Checks whether a game with the current code exists.
   * If it exists the code is output to the parent
   */
  async checkJoinCode() {
    if (this.code === null) return;

    this.loading = true;

    let res = await this.api.getGameExists(this.code);

    this.loading = false;

    if (res.isOK()) {
      this.icon.color = 'primary';
      this.icon.value = 'check';

      this.joinGameCode.emit(this.code);
    } else if (res.isNotFound()) {
      this.icon.color = 'warn';
      this.icon.value = 'close';
      this.input = '';

      this.snackBar.open('There exists no game with this code!', 'OK', {
        duration: 5000,
        panelClass: 'action-warn',
      });
    } else {
      this.icon.color = 'warn';
      this.icon.value = 'error';

      this.snackBar.open('Error loading game information!', 'OK', {
        duration: 5000,
        panelClass: 'action-warn',
      });
    }
  }
}
