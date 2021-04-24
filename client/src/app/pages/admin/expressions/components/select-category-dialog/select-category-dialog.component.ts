import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { Category } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-select-category-dialog',
  templateUrl: './select-category-dialog.component.html',
  styleUrls: ['./select-category-dialog.component.scss'],
})
export class SelectCategoryDialogComponent implements OnInit {
  category: Category | null = null;
  categories: Category[] = [];

  loading = false;

  constructor(
    private api: ApiService,
    private dialogRef: MatDialogRef<SelectCategoryDialogComponent>,
    private snackBar: MatSnackBar
  ) {}

  async ngOnInit() {
    this.loading = true;

    let res = await this.api.getAllCategories();

    this.loading = false;

    if (res.isOK() && res.value) {
      this.categories = res.value;
    } else {
      this.snackBar.open('Error loading categories!', 'OK', {
        panelClass: 'action-warn',
        duration: 10000,
      });

      this.dialogRef.close(null);
    }
  }

  get category_id() {
    return this.category?.id;
  }
}
