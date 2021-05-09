import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { User } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-player-list-dialog',
  templateUrl: './player-list-dialog.component.html',
  styleUrls: ['./player-list-dialog.component.scss'],
})
export class PlayerListDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      users: User[];
    }
  ) {}
}
