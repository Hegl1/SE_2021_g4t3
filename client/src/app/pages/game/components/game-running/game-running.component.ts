import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from 'src/app/core/auth/user.service';
import { GameService } from 'src/app/core/game/game.service';

@Component({
  selector: 'tg-game-running',
  templateUrl: './game-running.component.html',
  styleUrls: ['./game-running.component.scss'],
})
export class GameRunningComponent implements OnInit {
  time = 0;

  timer: any = null;

  constructor(public game: GameService, private user: UserService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.startTimer();
  }

  get teams() {
    return this.game.currentState?.teams || null;
  }

  get hostId() {
    return this.game.currentState?.host.id;
  }

  get currentUserId() {
    return this.user.user?.id;
  }

  get isCurrentUserHost() {
    if (!this.user.user || !this.game.currentState) return false;

    return this.user.user.id === this.game.currentState.host.id;
  }

  get currentPlayerUser() {
    return this.game.currentState?.running_data?.current_player || null;
  }

  /**
   * Starts the timer with the time from the running-data
   */
  startTimer() {
    if (this.game.currentState?.running_data) {
      if (this.game.currentState.running_data.round_pause_time === null) {
        this.updateTimer();

        this.timer = setInterval(() => {
          this.updateTimer();
        }, 1000);
      } else if (this.game.currentState.running_data.round_start_time !== null) {
        this.time =
          this.game.currentState.running_data.round_pause_time - this.game.currentState.running_data.round_start_time;
      } else {
        this.time = 0;
      }
    }
  }

  /**
   * Updates the current timer value
   */
  private updateTimer() {
    if (
      this.game.currentState?.running_data?.round_pause_time === null &&
      this.game.currentState?.running_data?.round_start_time !== null
    ) {
      this.time =
        this.game.currentState?.running_data?.round_start_time +
        this.game.currentState?.running_data?.total_time -
        Math.trunc(new Date().getTime() / 1000);

      if (this.time < 0) {
        this.time = 0;
        clearTimeout(this.timer);
        this.timer = null;
      }
    } else {
      if (
        this.game.currentState?.running_data?.round_pause_time &&
        this.game.currentState?.running_data?.round_start_time
      ) {
        this.time =
          this.game.currentState.running_data.round_pause_time - this.game.currentState.running_data.round_start_time;
      } else {
        this.time = 0;
      }

      clearTimeout(this.timer);
      this.timer = null;
    }
  }

  /**
   * Checks whether the authenticated user is in the team
   * with the given index
   *
   * @param index the teams index
   * @returns whether the authenticated user is in the team
   */
  isCurrentUsersTeam(index: number) {
    if (this.user.user?.id == null) return false;

    return this.game.isUsersTeam(this.user.user?.id, index);
  }

  /**
   * Leaves the current game
   */
  async leaveGame() {
    this.game.leave();
  }
}
