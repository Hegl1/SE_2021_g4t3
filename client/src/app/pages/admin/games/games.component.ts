import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { RunningGame } from 'src/app/core/api/ApiInterfaces';
import { GameCodePipe } from 'src/app/core/pipes/gamecode.pipe';

@Component({
  selector: 'tg-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.scss'],
})
export class GamesComponent implements OnInit {
  runningGames: RunningGame[] | null = null;

  error: string | null = null;
  loading = false;

  private _filter = '';

  constructor(private api: ApiService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.reload();
  }

  /**
   * Load the running games from the api
   */
  async reload() {
    this.error = null;
    this.loading = true;

    let res = await this.api.getAllGames();

    this.loading = false;

    if (!res.isOK()) {
      this.error = 'Error loading running games';
    }

    this.runningGames = res.value;
  }

  /**
   * Closes the game with the supplied code
   * @param code the code of the game
   */
  async closeGame(code: number) {
    let res = await this.api.deleteGame(code);

    if (res.isOK()) {
      this.snackBar.open('Game was closed successfully', 'OK', {
        duration: 5000,
      });

      await this.reload();
    } else {
      this.snackBar.open('Error closing game!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });
    }
  }

  set filter(filter: string) {
    let code = filter.replace(/-/g, '').substr(0, 8);

    this._filter = GameCodePipe.prototype.transform(code, false);
  }

  get filter() {
    return this._filter;
  }

  get filteredRunningGames() {
    if (!this.runningGames) return;

    return this.runningGames.filter((game) => {
      return game.code.toString().padStart(8, '0').startsWith(this.filter.replace(/-/g, ''));
    });
  }
}
