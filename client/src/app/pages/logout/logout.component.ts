import { Component, OnInit } from '@angular/core';
import { UserService } from 'src/app/core/auth/user.service';
import { GameService } from 'src/app/core/game/game.service';

@Component({
  selector: 'tg-logout',
  template: '',
  styleUrls: [],
})
export class LogoutComponent implements OnInit {
  constructor(private user: UserService, private game: GameService) {}

  async ngOnInit() {
    await this.game.leave(false);
    this.user.logoutReason('logout');
  }
}
