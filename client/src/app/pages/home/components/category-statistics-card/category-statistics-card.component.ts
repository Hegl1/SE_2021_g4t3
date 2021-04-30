import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/core/api/api.service';
import { CategoryStats } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-category-statistics-card',
  templateUrl: './category-statistics-card.component.html',
  styleUrls: ['./category-statistics-card.component.scss'],
})
export class CategoryStatisticsCardComponent implements OnInit {
  displayedColumns = ['category', 'number_incorrect', 'number_correct'];

  categoryStats: CategoryStats[] | null = null;

  error: string | null = null;

  constructor(private api: ApiService) {}

  async ngOnInit() {
    let res = await this.api.getCategoryStats();

    if (!res.isOK()) {
      this.error = 'Error loading category statistics';
      return;
    }

    this.categoryStats = res.value;
  }
}
