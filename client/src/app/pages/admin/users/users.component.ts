import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { ProfileDialogComponent } from 'src/app/components/profile-dialog/profile-dialog.component';
import { ApiService } from 'src/app/core/api/api.service';
import { User } from 'src/app/core/api/ApiInterfaces';
import { UserService } from 'src/app/core/auth/user.service';
import { EditUserDialogComponent } from './components/edit-user-dialog/edit-user-dialog.component';

@Component({
  selector: 'tg-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit {
  displayedColumns: string[] = ['id', 'username', 'role', 'admin'];
  users: MatTableDataSource<User> = new MatTableDataSource();

  @ViewChild(MatSort)
  set matSort(sort: MatSort) {
    this.users.sort = sort;
  }

  @ViewChild(MatPaginator)
  set paginator(paginator: MatPaginator) {
    this.users.paginator = paginator;
  }

  private _filter: string = '';

  loading = false;
  error: string | null = null;

  constructor(
    private api: ApiService,
    private user: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.reload();
  }

  set filter(filter: string) {
    this._filter = filter.trim().toLowerCase();

    this.users.filter = this._filter;

    if (this.users.paginator) {
      this.users.paginator.firstPage();
    }
  }

  get filter() {
    return this._filter;
  }

  /**
   * Load the users from the api
   */
  async reload() {
    this.loading = true;
    this.error = null;

    this.users.data = [];

    let res = await this.api.getAllUsers();

    if (res.isOK()) {
      if (res.value) this.users.data = res.value;
    } else {
      this.error = 'Error loading users';
    }

    this.loading = false;
  }

  /**
   * Checks whether the current user has the same id as the supplied one
   * @param id the id to check
   * @returns whether the user has the same id
   */
  isCurrentUser(id: number) {
    return this.user.user?.id === id;
  }

  /**
   * Openes the EditUserDialog for the user with the given id
   * or opens the dialog to create a new user, if id is null
   *
   * @param id the users id or null if a new user should be created
   */
  editUser(id: number | null) {
    this.dialog
      .open(EditUserDialogComponent, {
        data: {
          user_id: id,
        },
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.reload();
        }
      });
  }

  /**
   * Openes the users profile dialog
   *
   * @param id the users id
   */
  showProfile(id: number) {
    this.dialog.open(ProfileDialogComponent, {
      data: {
        user_id: id,
      },
    });
  }

  /**
   * Openes a confirm dialog before deleting the user with the given id
   * @param id the users id
   */
  deleteUser(id: number) {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Confirmation',
          content: `Are you sure you want to delete the user with id (${id})?`,
          warn: true,
        },
      })
      .afterClosed()
      .subscribe(async (confirmed: boolean) => {
        if (confirmed) {
          this.loading = true;

          let res = await this.api.deleteUser(id);

          this.loading = false;

          if (res.isOK()) {
            this.snackBar.open('User was deleted successfully!', 'OK', {
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
