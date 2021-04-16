import { Component, OnInit } from '@angular/core';
import { UserService } from 'src/app/core/auth/user.service';

@Component({
  selector: 'tg-logout',
  template: '',
  styleUrls: [],
})
export class LogoutComponent implements OnInit {
  constructor(private user: UserService) {}

  ngOnInit() {
    this.user.logoutReason('logout');
  }
}
