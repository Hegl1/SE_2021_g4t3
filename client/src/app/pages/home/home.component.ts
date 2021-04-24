import { Component } from '@angular/core';

@Component({
  selector: 'tg-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent {
  constructor() {}

  /**
   * Joins the game with the supplied code
   * @param code the game code
   */
  joinGameCode(code: number) {
    // TODO: connect user to game
    console.log('Connect user to game: ' + code);
  }
}
