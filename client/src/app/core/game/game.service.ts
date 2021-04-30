import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { ApiService } from '../api/api.service';
import { GameStatus, RunningGameState, Team, User } from '../api/ApiInterfaces';
import { WebsocketService } from '../api/websocket.service';

const INGAME_QUEUE = '/messagequeue/ingame';

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
    private snackBar: MatSnackBar,
    private dialog: MatDialog
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

    await this.websocket.connect(true).afterConnected();

    this._connected = true;
    this._currentState = res.value;

    this.websocket.subscribeQueue(`${INGAME_QUEUE}/` + this.code, (data) => {
      if (!this._currentState) return;

      switch (data.identifier) {
        case 'TEAM_UPDATE':
          let team_update = <{ teams: Team[] }>data.data;

          this._currentState.teams = team_update.teams;
          break;
        case 'READY_UPDATE':
          if (!this._currentState.waiting_data) return;
          let ready_update = <{ unassigned_players: User[]; ready_players: User[]; startable: boolean }>data.data;

          this._currentState.waiting_data.unassigned_players = ready_update.unassigned_players;
          this._currentState.waiting_data.ready_players = ready_update.ready_players;
          this._currentState.waiting_data.startable = ready_update.startable;
          break;
        case 'ERROR':
          let error = <{ message: string }>data.data;

          this.snackBar.open(error.message, 'OK', {
            duration: 10000,
            panelClass: 'action-warn',
          });
          break;
        case 'GAME_NOT_CONTINUEABLE':
          this.snackBar.open("The game can't be continued and was therefore terminated!", 'OK', {
            duration: 10000,
            panelClass: 'action-warn',
          });

          this.disconnect();

          break;
      }
    });
  }

  get code() {
    return this._currentState?.code || 0;
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

  /**
   * Checks whether the user is in the team
   * with the given index
   *
   * @param user_id the users id
   * @param team_index the teams index
   * @returns whether the authenticated user is in the team
   */
  isUsersTeam(user_id: number, team_index: number) {
    return this.currentState?.teams[team_index].players.some((user) => user.id === user_id) || false;
  }

  /**
   * leaves from the current game, disconnectes the websocket and
   * redirects to the homepage
   */
  async leave() {
    if (
      !(await this.dialog
        .open(ConfirmDialogComponent, {
          data: {
            title: 'Leave game',
            content: 'Do you really want to leave this game?',
            btnConfirm: 'Yes',
            btnDecline: 'No',
            warn: true,
          },
        })
        .afterClosed()
        .toPromise())
    ) {
      return;
    }

    let res = await this.api.leaveIngame(this.code);

    if (!res.isOK()) {
      this.snackBar.open('Error leaving game!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      return;
    }

    this.disconnect();
  }

  private disconnect() {
    this.router.navigateByUrl('/home');
    // TODO: disconnect websocket
  }
}
