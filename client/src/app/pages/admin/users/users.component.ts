import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
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

  constructor(private api: ApiService, private user: UserService) {}

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

    if (res.value) this.users.data = res.value;

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
    // TODO
  }
}
