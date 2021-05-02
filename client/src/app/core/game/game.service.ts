import { EventEmitter, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { ApiService } from '../api/api.service';
import { GameStatus, RunningGameState, Team, User } from '../api/ApiInterfaces';
import { WebsocketService } from '../api/websocket.service';
import { UserService } from '../auth/user.service';

const INGAME_QUEUE = '/messagequeue/ingame';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private _currentState: RunningGameState | null = null;
  private _connected = false;
  private teamQueue = false;

  readonly update = new EventEmitter<string>();

  constructor(
    private websocket: WebsocketService,
    private router: Router,
    private api: ApiService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private user: UserService
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

    let res_state = await this.api.getIngameState();

    if (!res_state.isOK() || !res_state.value) {
      let message = 'Error opening game!';

      if (res_state.isNotFound()) {
        message = 'You are not in a game!';
      }

      this.snackBar.open(message, 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      this.router.navigateByUrl('/home');
      return;
    }

    await this.websocket.connect(true).afterConnected(); // TODO: remove debug

    this._connected = true;
    this._currentState = res_state.value;

    this.subscribeGameQueue();

    if (this._currentState.running_data) {
      this.subscribeTeamQueue();
    }

    let res_join = await this.api.joinGame(res_state.value?.code);

    if (!res_join.isOK()) {
      this.snackBar.open('Error joining game!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      this.router.navigateByUrl('/home');
      return;
    }
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
   * Returns the users team index
   *
   * @param user_id the users id
   * @returns the users team index or null if not found
   */
  getUsersTeam(user_id: number) {
    if (!this.currentState) return null;

    for (let i = 0; i < this.currentState.teams.length; i++) {
      if (this.isUsersTeam(user_id, i)) {
        return this.currentState.teams[i].index;
      }
    }

    return null;
  }

  /**
   * Subscribes to the game queue and handles the responses
   */
  subscribeGameQueue() {
    this.websocket.subscribeQueue(`${INGAME_QUEUE}/${this.code}`, (data) => {
      if (!this._currentState) return;

      switch (data.identifier) {
        case 'FULL_INFO':
          let full_info = <RunningGameState>data.data;

          this._currentState = full_info;

          if (full_info.running_data && !this.teamQueue) {
            this.subscribeTeamQueue();
          }

          break;
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
        case 'RUNNING_DATA':
          this._currentState.running_data = data.data;
          this.update.emit('RUNNING_DATA');

          break;
        case 'SCORE_UPDATE':
          let score_update = <{ index: number; score: number }>data.data;

          this._currentState.teams[score_update.index].score = score_update.score;
          break;
      }
    });
  }

  /**
   * Subscribes to the team queue and handles the responses
   */
  subscribeTeamQueue() {
    let team;
    if (this.user.user && (team = this.getUsersTeam(this.user.user.id)) !== null) {
      this.websocket.subscribeQueue(`${INGAME_QUEUE}/team/${this.code}/${team}`, (data) => {
        if (!this._currentState) return;

        this._currentState.running_data = data.data;
        this.update.emit('RUNNING_DATA');
      });
    }
  }

  /**
   * Confirms the answer of a team
   *
   * @param type the type of the confirmation
   */
  async confirmAnswer(type: 'CORRECT' | 'WRONG' | 'INVALID') {
    if (!this.currentState) return;

    let res = await this.api.confirmIngame(this.currentState.code, type);

    if (!res.isOK()) {
      this.snackBar.open('Error confirming answer!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      return;
    }
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

    this._connected = false;
    this._currentState = null;
    this.teamQueue = false;
    // TODO: disconnect websocket
  }
}
