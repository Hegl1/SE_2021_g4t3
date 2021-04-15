import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { ApiService } from 'src/app/core/api/api.service';
import { User } from 'src/app/core/api/ApiInterfaces';
import { UserService } from 'src/app/core/auth/user.service';

@Component({
  selector: 'tg-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements AfterViewInit, OnInit {
  displayedColumns: string[] = ['id', 'username', 'role', 'admin'];
  users: MatTableDataSource<User> = new MatTableDataSource();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

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

  ngAfterViewInit() {
    this.users.sort = this.sort;
    this.users.paginator = this.paginator;
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
    return false && this.user.user?.id === id; // TODO: revert
  }

  /**
   * Openes the EditUserDialog with the parameter to create one
   */
  createUser() {
    // TODO
  }

  /**
   * Openes the EditUserDialog for the user with the given id
   * @param id the users id
   */
  editUser(id: number) {
    // TODO
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
