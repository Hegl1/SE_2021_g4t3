import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PlayerListDialogComponent } from 'src/app/components/player-list-dialog/player-list-dialog.component';
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

  constructor(private api: ApiService, private dialog: MatDialog) {}

  async ngOnInit() {
    let res = await this.api.getGlobalStats();

    if (!res.isOK()) {
      this.error = 'Error loading global statistics';
      return;
    }

    this.globalStats = res.value;
  }

  /**
   * Returns a list of users, that have the most wins.
   * If there are more than <i>amount</i> users, truncate is set to true.
   *
   * @param amount the amount of users to contain
   * @returns the players and if they should be truncated
   */
  getMostWon(amount = 3) {
    if (this.globalStats === null) return null;

    return {
      players: this.globalStats.mostGamesWon,
      truncate: (this.globalStats.mostGamesWon.length || 0) > amount,
    };
  }

  showMostWonList() {
    this.dialog.open(PlayerListDialogComponent, {
      data: {
        users: this.globalStats?.mostGamesWon || [],
      },
    });
  }
}
