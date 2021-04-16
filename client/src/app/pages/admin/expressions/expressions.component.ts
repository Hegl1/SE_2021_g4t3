import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { InputDialogComponent } from 'src/app/components/input-dialog/input-dialog.component';
import { ApiService } from 'src/app/core/api/api.service';
import { Category, CategoryInfo } from 'src/app/core/api/ApiInterfaces';
import { AddExpressionsDialogComponent } from './components/add-expressions-dialog/add-expressions-dialog.component';

@Component({
  selector: 'tg-expressions',
  templateUrl: './expressions.component.html',
  styleUrls: ['./expressions.component.scss'],
})
export class ExpressionsComponent implements AfterViewInit, OnInit {
  displayedColumns: string[] = ['id', 'name', 'expressions', 'admin'];
  categories: MatTableDataSource<CategoryInfo> = new MatTableDataSource();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  loading = false;
  error: string | null = null;

  constructor(private api: ApiService, private dialog: MatDialog, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.reload();
  }

  ngAfterViewInit() {
    this.categories.sort = this.sort;
    this.categories.paginator = this.paginator;
  }

  /**
   * Load the users from the api
   */
  async reload() {
    this.loading = true;
    this.error = null;

    this.categories.data = [];

    let res = await this.api.getAllCategoriesInfo();

    if (res.isOK()) {
      if (res.value) this.categories.data = res.value;
    } else {
      this.error = 'Error loading categories';
    }

    this.loading = false;
  }

  /**
   * Filters the user list
   * @param event
   */
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.categories.filter = filterValue.trim().toLowerCase();

    if (this.categories.paginator) {
      this.categories.paginator.firstPage();
    }
  }

  /**
   * Opens an input field for the name of the new category and saves it
   */
  createCategory() {
    this.dialog
      .open(InputDialogComponent, {
        data: {
          title: 'Create category',
          label: 'Name',
        },
      })
      .afterClosed()
      .subscribe(async (value: string | null) => {
        if (value) {
          this.loading = true;

          let res = await this.api.createCategory(value.trim());

          this.loading = false;

          if (res.isOK()) {
            this.snackBar.open('Category was created successfully!', 'OK', {
              duration: 5000,
            });

            // TODO: open confirm to ask if wants to add expressions

            await this.reload();

            return;
          }

          let error = null;

          if (res.isConflict()) {
            error = 'A category with this name exists already';
          }

          this.snackBar.open(error || 'An error occured!', 'OK', {
            panelClass: 'action-warn',
            duration: 10000,
          });
        }
      });
  }

  /**
   * Opens an input field to insert new expressions
   * for the supplied category
   *
   * @param category_id the id of the category
   */
  addExpressions(category_id: number) {
    this.dialog
      .open(AddExpressionsDialogComponent, {
        data: {
          category_id: category_id,
        },
        width: '500px',
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.reload();
        }
      });
  }

  /**
   * Openes a confirm dialog before deleting the category with the given id
   * @param id the users id
   */
  deleteCategory(id: number) {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Confirmation',
          content: `Are you sure you want to delete the category with id (${id})?`,
          warn: true,
        },
      })
      .afterClosed()
      .subscribe(async (confirmed: boolean) => {
        if (confirmed) {
          this.loading = true;

          let res = await this.api.deleteCategory(id);

          this.loading = false;

          if (res.isOK()) {
            this.snackBar.open('Category was deleted successfully!', 'OK', {
              duration: 5000,
            });

            await this.reload();
          } else {
            this.snackBar.open('An error occured!', 'OK', {
              panelClass: 'action-warn',
              duration: 10000,
            });
          }
        }
      });
  }
}
