import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'tg-input-dialog',
  templateUrl: './input-dialog.component.html',
  styleUrls: ['./input-dialog.component.scss'],
})
export class InputDialogComponent {
  value = '';

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      title: string;
      label?: string;
      default?: string;
      btnConfirm?: string;
      btnDecline?: string;
    }
  ) {
    if (data.default != null) {
      this.value = data.default;
    }
  }

  get btnConfirm() {
    return this.data.btnConfirm ? this.data.btnConfirm : 'Save';
  }

  get btnDecline() {
    return this.data.btnDecline ? this.data.btnDecline : 'Cancel';
  }
}
