import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'tg-help-dialog',
  templateUrl: './help-dialog.component.html',
  styleUrls: ['./help-dialog.component.scss'],
})
export class HelpDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      key: string;
    }
  ) {}

  get title() {
    switch (this.data.key) {
      case 'IMPORT_EXPRESSIONS':
        return 'Import expressions';
      case 'DICE_CONNECTION':
        return 'TimeFlip connection';
      case 'DICE_MAPPING':
        return 'TimeFlip mapping';
    }

    return '?';
  }
}
