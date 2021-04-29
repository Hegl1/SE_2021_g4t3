import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ApiService } from 'src/app/core/api/api.service';

@Component({
  selector: 'tg-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent {
  constructor(private api: ApiService, private snackBar: MatSnackBar, private router: Router) {}

  /**
   * Joins the game with the supplied code
   * @param code the game code
   */
  async joinGameCode(code: number) {
    console.log('Connecting user to game: ' + code);

    let res = await this.api.joinGame(code);

    if (!res.isOK()) {
      let message = 'Error joining game!';

      if (res.isConflict()) {
        message = 'You are already in a game!';
      }

      this.snackBar.open(message, 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });
      return;
    }

    this.router.navigateByUrl('/game');
  }
}
