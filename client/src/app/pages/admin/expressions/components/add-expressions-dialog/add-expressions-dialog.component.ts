import { ENTER } from '@angular/cdk/keycodes';
import { Component, Inject } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { Category } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-add-expressions-dialog',
  templateUrl: './add-expressions-dialog.component.html',
  styleUrls: ['./add-expressions-dialog.component.scss'],
})
export class AddExpressionsDialogComponent {
  readonly separatorKeysCodes: number[] = [ENTER];

  expressions: string[] = [];

  saving = false;
  error: string | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      category: Category;
    },
    private api: ApiService,
    private dialogRef: MatDialogRef<AddExpressionsDialogComponent>,
    private snackBar: MatSnackBar
  ) {}

  /**
   * Adds a chip to the expressions
   * @param event
   */
  addExpression(event: MatChipInputEvent) {
    const input = event.input;
    const value = (event.value || '').trim();

    if (value && this.expressions.indexOf(value) === -1) {
      this.expressions.push(value);
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  /**
   * Removes the supplied expression from the list
   * @param expression
   */
  removeExpression(expression: string) {
    const index = this.expressions.indexOf(expression);

    if (index >= 0) {
      this.expressions.splice(index, 1);
    }
  }

  /**
   * Saves the created expressions to the database
   */
  async save() {
    if (this.expressions.length === 0) return;

    this.saving = true;
    this.error = null;
    this.dialogRef.disableClose = true;

    let res = await this.api.importExpressionsForCategory(this.data.category.id, this.expressions);

    this.saving = false;
    this.dialogRef.disableClose = false;

    if (res.isOK()) {
      this.snackBar.open(`Expressions where created successfully!`, 'OK', {
        duration: 5000,
      });
      this.dialogRef.close(true);
    } else {
      this.error = res.error || 'Error saving expressions!';
    }
  }
}
