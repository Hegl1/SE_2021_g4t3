import { Component } from '@angular/core';
import { UserService } from 'src/app/core/auth/user.service';
import { GameService } from 'src/app/core/game/game.service';

@Component({
  selector: 'tg-game-finished',
  templateUrl: './game-finished.component.html',
  styleUrls: ['./game-finished.component.scss'],
})
export class GameFinishedComponent {
  constructor(public game: GameService, private user: UserService) {}

  get currentUserId() {
    return this.user.user?.id;
  }
}
