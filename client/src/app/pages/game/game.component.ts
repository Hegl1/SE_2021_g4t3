import { Component, OnInit } from '@angular/core';
import { GameStatus } from 'src/app/core/api/ApiInterfaces';
import { GameService } from 'src/app/core/game/game.service';

@Component({
  selector: 'tg-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss'],
})
export class GameComponent implements OnInit {
  constructor(private game: GameService) {}

  async ngOnInit() {
    if (!this.game.connected) {
      await this.game.connectToGame();
    }
  }

  get status() {
    return this.game.currentState?.status || null;
  }

  get statusString() {
    switch (this.status) {
      case GameStatus.Waiting:
        return 'Waiting';
      case GameStatus.Running:
        return 'LIVE';
      case GameStatus.Finished:
        return 'Finished';
    }

    return null;
  }

  get isWaiting() {
    return this.status === GameStatus.Waiting;
  }
  get isRunning() {
    return this.status === GameStatus.Running;
  }
  get isFinished() {
    return this.status === GameStatus.Finished;
  }
}
