import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ApiService } from '../api/api.service';
import { GameStatus, RunningGameState } from '../api/ApiInterfaces';
import { WebsocketService } from '../api/websocket.service';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private _currentState: RunningGameState | null = null;
  private _connected = false;

  constructor(
    private websocket: WebsocketService,
    private router: Router,
    private api: ApiService,
    private snackBar: MatSnackBar
  ) {}

  /**
   * Connects the user to his running game:
   * - Retrieves the current state
   * - Connects to the websocket
   * - Listenes to the message-queues
   */
  async connectToGame() {
    this._connected = false;
    this._currentState = null;

    let res = await this.api.getIngameState();

    if (!res.isOK()) {
      let message = 'Error opening game!';

      if (res.isNotFound()) {
        message = 'You are not in a game!';
      }

      this.snackBar.open(message, 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      this.router.navigateByUrl('/home');
      return;
    }

    await this.websocket.connect().afterConnected();

    this._connected = true;
    this._currentState = res.value;

    // TODO: listen to message-queues
  }

  get currentState() {
    return this._currentState;
  }
  get connected() {
    return this._connected;
  }
  get totalPlayers() {
    if (!this._currentState) return 0;

    let total = this._currentState.teams.reduce((acc, team) => acc + team.players.length, 0);

    if (this._currentState.status === GameStatus.Waiting && this._currentState.waiting_data) {
      total += this._currentState.waiting_data.unassigned_players.length;
    }

    return total;
  }
}
