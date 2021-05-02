import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/core/api/api.service';
import { UserStats } from 'src/app/core/api/ApiInterfaces';
import { UserService } from 'src/app/core/auth/user.service';

@Component({
  selector: 'tg-personal-statistics-card',
  templateUrl: './personal-statistics-card.component.html',
  styleUrls: ['./personal-statistics-card.component.scss'],
})
export class PersonalStatisticsCardComponent implements OnInit {
  userStats: UserStats | null = null;

  wonGames: number = 0;
  lostGames: number = 0;

  error: string | null = null;

  constructor(private api: ApiService, private user: UserService) {}

  async ngOnInit() {
    if (this.user.user === null) return;

    let res = await this.api.getUserStats(this.user.user?.id);

    if (!res.isOK()) {
      this.error = 'Error loading personal statistics';
      return;
    }

    this.userStats = res.value;

    this.userStats?.won_games.forEach((element) => (this.wonGames += element.amount));
    this.userStats?.lost_games.forEach((element) => (this.lostGames += element.amount));
  }
}
