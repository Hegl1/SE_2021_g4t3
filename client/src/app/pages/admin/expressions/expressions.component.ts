import { Component, ElementRef, EventEmitter, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Subscription } from 'rxjs';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { HelpDialogComponent } from 'src/app/components/help-dialog/help-dialog.component';
import { InputDialogComponent } from 'src/app/components/input-dialog/input-dialog.component';
import { ApiService } from 'src/app/core/api/api.service';
import { Category, CategoryInfo } from 'src/app/core/api/ApiInterfaces';
import { ApiResponse } from 'src/app/core/api/ApiResponse';
import { FileHelper } from 'src/app/core/files/file-helper';
import { AddExpressionsDialogComponent } from './components/add-expressions-dialog/add-expressions-dialog.component';
import { SelectCategoryDialogComponent } from './components/select-category-dialog/select-category-dialog.component';
import { ShowExpressionsOverlayComponent } from './components/show-expressions-overlay/show-expressions-overlay.component';

@Component({
  selector: 'tg-expressions',
  templateUrl: './expressions.component.html',
  styleUrls: ['./expressions.component.scss'],
})
export class ExpressionsComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'expressions', 'admin'];
  categories: MatTableDataSource<CategoryInfo> = new MatTableDataSource();

  @ViewChild(MatSort)
  set matSort(sort: MatSort) {
    this.categories.sort = sort;
  }

  @ViewChild(MatPaginator)
  set paginator(paginator: MatPaginator) {
    this.categories.paginator = paginator;
  }

  @ViewChild('importFilePicker') importFilePicker!: ElementRef;

  private _filter: string = '';

  loading = false;
  saving = false;
  error: string | null = null;

  private importFileObservable = new EventEmitter<File>();
  private importFileSubscriptions: Subscription[] = [];

  constructor(private api: ApiService, private dialog: MatDialog, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.reload();
  }

  set filter(filter: string) {
    this._filter = filter.trim().toLowerCase();

    this.categories.filter = this._filter;

    if (this.categories.paginator) {
      this.categories.paginator.firstPage();
    }
  }

  get filter() {
    return this._filter;
  }

  /**
   * Notifies all subscribers about the new selected file for imports
   * @param event
   */
  setImportFile(event: EventTarget | null) {
    if (event === null) return;

    let files = (<HTMLInputElement>event).files;

    if (files?.length !== 1) return;

    this.importFileObservable.emit(files[0]);
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
          this.saving = true;

          let res = await this.api.createCategory(value.trim());

          this.saving = false;

          if (res.isOK()) {
            this.snackBar.open('Category was created successfully!', 'OK', {
              duration: 5000,
            });

            // ask user if he want to add expressions
            if (
              await this.dialog
                .open(ConfirmDialogComponent, {
                  data: {
                    title: 'Add expressions',
                    content: 'Do you want to add expressions to the created category?',
                    btnConfirm: 'Yes',
                    btnDecline: 'No',
                  },
                })
                .afterClosed()
                .toPromise()
            ) {
              await this.dialog
                .open(AddExpressionsDialogComponent, {
                  data: {
                    category: res.value,
                  },
                  width: '500px',
                })
                .afterClosed()
                .toPromise();
            }

            await this.reload();

            return;
          }

          let error = null;

          if (res.isConflict()) {
            error = 'A category with this name exists already';
          }

          this.snackBar.open(error || res.error || 'An error occured!', 'OK', {
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
   * @param category the category
   */
  addExpressions(category: Category) {
    // TODO:
    this.dialog
      .open(AddExpressionsDialogComponent, {
        data: {
          category: category,
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
          this.saving = true;

          let res = await this.api.deleteCategory(id);

          this.saving = false;

          if (res.isOK()) {
            this.snackBar.open('Category was deleted successfully!', 'OK', {
              duration: 5000,
            });

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

  /**
   * Opens a file picker to select a .csv or .json file
   * to import
   *
   * @param type whether to import only expressions or categories with expressions
   */
  async import(type: 'expressions' | 'categories_and_expressions', category_id: number | null = null) {
    if (!this.importFilePicker) return;

    this.importFileSubscriptions.forEach((sub) => sub.unsubscribe());

    if (type === 'expressions' && category_id === null) {
      category_id = await this.dialog.open(SelectCategoryDialogComponent).afterClosed().toPromise();

      if (category_id == null) {
        return;
      }
    }

    let subscription = this.importFileObservable.subscribe(async (file) => {
      this.saving = true;

      try {
        let data = await FileHelper.readFile(file);

        let values;

        if (file.name.endsWith('.json')) {
          values = JSON.parse(data);
        } else if (file.name.endsWith('.csv')) {
          let csv = FileHelper.parseCSV(data);

          if (type === 'categories_and_expressions') {
            let category_mapping: any = {};

            csv.forEach((row) => {
              if (row.length !== 2) {
                throw new Error('File format is invalid');
              }

              if (typeof category_mapping[row[0]] === 'undefined') {
                category_mapping[row[0]] = {
                  category: row[0],
                  expressions: [row[1]],
                };
              } else {
                category_mapping[row[0]].expressions.push(row[1]);
              }
            });

            values = Object.values(category_mapping);
          } else {
            values = csv.map((row) => row[0]);
          }
        } else {
          throw new Error('Invalid file extension');
        }

        try {
          let res: ApiResponse<any>;

          if (type === 'expressions') {
            res = await this.api.importExpressionsForCategory(category_id!, values);
          } else {
            res = await this.api.importExpressions(values);
          }

          if (res.isOK()) {
            this.snackBar.open('The expressions where imported successfully!', 'OK', {
              duration: 5000,
            });

            this.reload();
          } else if (res.isNotFound()) {
            throw new Error('The category was not found');
          } else if (res.isBadRequest()) {
            throw new Error('Invalid format (see "Help" for more information)');
          } else {
            throw new Error(res.error || undefined);
          }
        } catch (e) {
          this.snackBar.open('Error importing data' + (e.message ? ': ' + e.message : ''), 'OK', {
            duration: 10000,
            panelClass: 'action-warn',
          });
        }
      } catch (e) {
        this.snackBar.open('Error reading file' + (e.message ? ': ' + e.message : ''), 'OK', {
          duration: 10000,
          panelClass: 'action-warn',
        });
      }

      this.saving = false;

      subscription.unsubscribe();
    });
    this.importFileSubscriptions.push(subscription);

    this.importFilePicker.nativeElement.click();
  }

  async showExpressions(category_id: number) {
    if (
      await this.dialog
        .open(ShowExpressionsOverlayComponent, {
          data: {
            category_id: category_id,
          },
          width: '550px',
        })
        .afterClosed()
        .toPromise()
    ) {
      await this.reload();
    }
  }

  /**
   * Shows a help dialog for importing expressions
   */
  showHelpImport() {
    this.dialog.open(HelpDialogComponent, {
      data: {
        key: 'IMPORT_EXPRESSIONS',
      },
    });
  }
}
