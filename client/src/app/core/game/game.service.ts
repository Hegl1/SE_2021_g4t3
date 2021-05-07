import { EventEmitter, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { ApiService } from '../api/api.service';
import { FinishedData, GameStatus, RunningGameState, Team, User } from '../api/ApiInterfaces';
import { WebsocketService } from '../api/websocket.service';
import { UserService } from '../auth/user.service';
import { ConfigService } from '../config/config.service';

const INGAME_QUEUE = '/messagequeue/ingame';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private _currentState: RunningGameState | null = null;
  private teamQueueConnected = false;
  private _finishedData: FinishedData | null = null;
  private _ignoreQueues = false;

  readonly update = new EventEmitter<string>();

  constructor(
    private websocket: WebsocketService,
    private router: Router,
    private api: ApiService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private user: UserService,
    private config: ConfigService
  ) {}

  /**
   * Connects the user to his running game:
   * - Retrieves the current state
   * - Connects to the websocket
   * - Listenes to the message-queues
   */
  async connectToGame() {
    this._currentState = null;
    this.teamQueueConnected = false;
    this._finishedData = null;
    this._ignoreQueues = false;

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

    try {
      await this.websocket.connect().afterConnected();

      this._currentState = res_state.value;

      this.subscribeGameQueue();

      if (this._currentState.running_data) {
        this.subscribeTeamQueue();
      }
    } catch (e) {
      console.error('Error connecting to websocket!');

      this.snackBar.open('Error opening connection to server!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      this.router.navigateByUrl('/home');
    }
  }

  get code() {
    return this._currentState?.code || 0;
  }
  get currentState() {
    return this._currentState;
  }
  get finishedData() {
    return this._finishedData;
  }
  get connected() {
    return this.websocket.isConnected;
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
  async subscribeGameQueue() {
    try {
      await this.websocket.subscribeQueue(`${INGAME_QUEUE}/${this.code}`, (data) => {
        if (!this._currentState || this._ignoreQueues) return;

        switch (data.identifier) {
          case 'FULL_INFO':
            let full_info = <RunningGameState>data.data;

            this._currentState = full_info;

            if (full_info.running_data && !this.teamQueueConnected) {
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

            this.reset();

            break;
          case 'RUNNING_DATA':
            this._currentState.running_data = data.data;
            this.update.emit('RUNNING_DATA');

            break;
          case 'SCORE_UPDATE':
            let score_update = <{ index: number; score: number }>data.data;

            this._currentState.teams[score_update.index].score = score_update.score;
            break;
          case 'REGULAR_FINISH':
          case 'EARLY_FINISH':
            this._currentState.running_data = null;
            this._currentState.status = GameStatus.Finished;

            this._finishedData = data.data;

            this.disconnectWebsocket();

            if (data.identifier === 'EARLY_FINISH') {
              this.snackBar.open(
                'The game was terminated early, since there are no more unused expressions in this category',
                'OK',
                {
                  duration: 5000,
                }
              );
            }

            break;
          case 'BAT_UPDATE':
            this._currentState.dice_info.level = data.data.batLevel;

            if (data.data.batLevel <= this.config.get('critical_battery_level', 10)) {
              this.snackBar.open('Battery level is critical!', 'OK', {
                duration: 10000,
                panelClass: 'action-warn',
              });
            }
            break;
          case 'CONN_UPDATE':
            this._currentState.dice_info.connected = data.data.connectionStatus;

            if (!data.data.connectionStatus) {
              this.snackBar.open('Connection to dice lost!', 'OK', {
                duration: 10000,
                panelClass: 'action-warn',
              });
            }
            break;
        }
      });
    } catch (e) {
      console.error('Error connecting to game queue: ', e);
    }
  }

  /**
   * Subscribes to the team queue and handles the responses
   */
  async subscribeTeamQueue() {
    try {
      let team;
      if (this.user.user && (team = this.getUsersTeam(this.user.user.id)) !== null) {
        this.websocket.subscribeQueue(`${INGAME_QUEUE}/team/${this.code}/${team}`, (data) => {
          if (!this._currentState || this._ignoreQueues) return;

          switch (data.identifier) {
            case 'RUNNING_DATA':
              this._currentState.running_data = data.data;
              this.update.emit('RUNNING_DATA');
          }
        });

        this.teamQueueConnected = true;
      }
    } catch (e) {
      console.error('Error connecting to team queue: ', e);
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
   *
   * @param confirm whether to ask the user for confirmation
   */
  async leave(confirm = true) {
    if (
      confirm &&
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

    this._ignoreQueues = true;

    let res = await this.api.leaveIngame(this.code);

    if (!res.isOK() && !res.isNotFound()) {
      this.snackBar.open('Error leaving game!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });

      this._ignoreQueues = false;

      return;
    }

    this.reset();
  }

  /**
   * Resets the game service
   * @param redirect whether to redirect to the homepage or not
   */
  reset(redirect = true) {
    this._currentState = null;
    this.teamQueueConnected = false;
    this._finishedData = null;
    this._ignoreQueues = false;

    this.disconnectWebsocket();

    if (redirect) {
      this.router.navigateByUrl('/home');
    }
  }

  /**
   * Disconnects from the websocket
   */
  private disconnectWebsocket() {
    this.websocket.disconnect();
  }
}
