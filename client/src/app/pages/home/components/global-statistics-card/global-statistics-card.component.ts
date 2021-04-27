import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/core/api/api.service';
import { GlobalStats } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-global-statistics-card',
  templateUrl: './global-statistics-card.component.html',
  styleUrls: ['./global-statistics-card.component.scss'],
})
export class GlobalStatisticsCardComponent implements OnInit {
  globalStats: GlobalStats | null = null;

  error: string | null = null;

  constructor(private api: ApiService) {}

  async ngOnInit() {
    let res = await this.api.getGlobalStats();

    if (!res.isOK()) {
      this.error = 'Error loading global statistics';
      return;
    }

    this.globalStats = res.value;
  }

  /**
   * Generates a string of users, that have the most wins with a
   * maximum <i>amount</i> of usernames.
   *
   * @param amount the amount of users to contain
   * @returns the generated string
   */
  getMostWonPlayersString(amount = 3) {
    if (this.globalStats === null) return null;

    return (
      this.globalStats.mostGamesWon
        .slice(0, Math.min(this.globalStats.mostGamesWon.length, amount))
        .map((element) => element.username)
        .join(', ') + (this.globalStats.mostGamesWon.length > amount ? ', ...' : '')
    );
  }
}
