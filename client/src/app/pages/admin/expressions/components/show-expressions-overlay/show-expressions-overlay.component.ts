import { Expression } from '@angular/compiler';
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { ApiService } from 'src/app/core/api/api.service';
import { Category } from 'src/app/core/api/ApiInterfaces';
import { AddExpressionsDialogComponent } from '../add-expressions-dialog/add-expressions-dialog.component';

@Component({
  selector: 'tg-show-expressions-overlay',
  templateUrl: './show-expressions-overlay.component.html',
  styleUrls: ['./show-expressions-overlay.component.scss'],
})
export class ShowExpressionsOverlayComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'admin'];
  @ViewChild(MatSort)
  set matSort(sort: MatSort) {
    this.expressions.sort = sort;
  }

  @ViewChild(MatPaginator)
  set paginator(paginator: MatPaginator) {
    this.expressions.paginator = paginator;
  }

  category: Category | null = null;
  expressions: MatTableDataSource<Expression> = new MatTableDataSource();

  private _filter: string = '';

  saving = false;
  loading = false;

  changes = false;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      category_id: number;
    },
    private api: ApiService,
    private dialogRef: MatDialogRef<ShowExpressionsOverlayComponent>,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.reload();

    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => {
      this.dialogRef.close(this.changes);
    });
  }

  set filter(filter: string) {
    this._filter = filter.trim().toLowerCase();

    this.expressions.filter = this._filter;

    if (this.expressions.paginator) {
      this.expressions.paginator.firstPage();
    }
  }

  get filter() {
    return this._filter;
  }

  /**
   * Load the category information and expressions from the api
   */
  async reload() {
    this.loading = true;

    this.expressions.data = [];

    let res = await Promise.all([
      this.api.getCategory(this.data.category_id),
      this.api.getExpressionsForCategory(this.data.category_id),
    ]);

    this.loading = false;

    if (!res[0].isOK() || !res[1].isOK()) {
      this.snackBar.open('Error loading category- and/or expression-information!', 'OK', {
        panelClass: 'action-warn',
        duration: 10000,
      });
      this.dialogRef.close();
      return;
    }

    this.category = res[0].value;
    this.expressions.data = res[1].value || [];
  }

  /**
   * Opens an input field to insert new expressions for the category
   */
  addExpressions() {
    this.dialog
      .open(AddExpressionsDialogComponent, {
        data: {
          category: this.category,
        },
        width: '500px',
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.changes = true;
          this.reload();
        }
      });
  }

  /**
   * Openes a confirm dialog before deleting the category with the given id
   * @param id the users id
   */
  deleteExpression(id: number) {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Confirmation',
          content: `Are you sure you want to delete the expression with id (${id})?`,
          warn: true,
        },
      })
      .afterClosed()
      .subscribe(async (confirmed: boolean) => {
        if (confirmed) {
          this.saving = true;

          let res = await this.api.deleteExpression(id);

          this.saving = false;

          if (res.isOK()) {
            this.snackBar.open('Expression was deleted successfully!', 'OK', {
              duration: 5000,
            });

            this.changes = true;
            await this.reload();
          } else {
            this.snackBar.open(res.error || 'An error occured!', 'OK', {
              panelClass: 'action-warn',
              duration: 10000,
            });
          }
        }
      });
  }
}
