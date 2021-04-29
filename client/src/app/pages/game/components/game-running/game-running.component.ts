import { Component, OnInit } from '@angular/core';
import { UserService } from 'src/app/core/auth/user.service';
import { GameService } from 'src/app/core/game/game.service';

@Component({
  selector: 'tg-game-running',
  templateUrl: './game-running.component.html',
  styleUrls: ['./game-running.component.scss'],
})
export class GameRunningComponent implements OnInit {
  time = 0;

  private timer: any = null;

  constructor(public game: GameService, private user: UserService) {}

  ngOnInit() {
    this.startTimer();
  }

  /**
   * Starts the timer with the time from the running-data
   */
  startTimer() {
    if (this.game.currentState?.running_data?.running) {
      this.updateTimer();

      this.timer = setInterval(() => {
        this.updateTimer();
      }, 1000);
    }
  }

  /**
   * Updates the current timer value
   */
  private updateTimer() {
    if (this.game.currentState?.running_data?.running) {
      this.time =
        this.game.currentState?.running_data?.round_start_time +
        this.game.currentState?.running_data?.total_time -
        Math.trunc(new Date().getTime() / 1000);

      if (this.time < 0) {
        this.time = 0;
        clearTimeout(this.timer);
      }
    } else {
      clearTimeout(this.timer);
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
}
