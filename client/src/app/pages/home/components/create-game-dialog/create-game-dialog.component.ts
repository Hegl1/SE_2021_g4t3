import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { Category } from 'src/app/core/api/ApiInterfaces';

@Component({
  selector: 'tg-create-game-dialog',
  templateUrl: './create-game-dialog.component.html',
  styleUrls: ['./create-game-dialog.component.scss'],
})
export class CreateGameDialogComponent implements OnInit {
  loading = false;
  saving = false;
  error: string | null = null;

  categories: Category[] | null = null;

  createForm: FormGroup;
  mapping = null; // TODO:

  constructor(
    private api: ApiService,
    private dialogRef: MatDialogRef<CreateGameDialogComponent>,
    private snackBar: MatSnackBar,
    private fb: FormBuilder
  ) {
    this.createForm = this.fb.group({
      number_teams: [null, [Validators.required, Validators.min(2), Validators.pattern('^[0-9]*$')]],
      maximum_score: [null, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]*$')]],
      category: [null, [Validators.required]],
      timeflip_code: [null, [Validators.required]],
    });
  }

  async ngOnInit() {
    this.loading = true;

    let res = await this.api.getAllCategories();

    this.loading = false;

    if (res.isOK()) {
      this.categories = res.value;
    } else {
      this.dialogRef.close(null);
      this.snackBar.open('Error loading categories!', 'OK', {
        duration: 5000,
        panelClass: 'action-warn',
      });
    }
  }

  /**
   * Checks whether the dice is available or not
   */
  async checkTimeFlip() {
    // TODO:
  }

  /**
   * Creates the game with the supplied data
   */
  async save() {
    this.createForm.markAllAsTouched();
    this.createForm.updateValueAndValidity();

    if (this.createForm.invalid) return;

    this.saving = true;
    this.error = null;
    this.createForm.disable();
    this.dialogRef.disableClose = true;

    let timeflip_code = this.createForm.get('timeflip_code')?.value;
    let category = this.createForm.get('category')?.value;
    let maximum_score = this.createForm.get('maximum_score')?.value;
    let number_teams = this.createForm.get('number_teams')?.value;

    let res = await this.api.createGame(timeflip_code, category, maximum_score, number_teams, null);

    this.saving = false;
    this.createForm.enable();
    this.dialogRef.disableClose = false;

    if (res.isOK()) {
      this.dialogRef.close(res.value);
      return;
    } else {
      this.error = 'Error creating game'; // TODO: error
    }
  }

  getFormError(key: string) {
    let field = this.createForm?.get(key);
    if (field?.invalid) {
      if (field.hasError('required')) {
        return 'This field is required';
      }
      if (field.hasError('min')) {
        if (key === 'number_teams') {
          return 'A minimum of 2 teams has to be supplied';
        } else if (key === 'maximum_score') {
          return 'A minimum of 1 points must be set';
        }
        return 'The number is too small';
      }
      if (field.hasError('pattern')) {
        return 'A positive integer must be supplied';
      }
    }

    return null;
  }

  /**
   * Shows a help dialog for connecting to a dice
   */
  showHelpConnection() {
    // TODO: show help
    throw new Error('Not implemented');
  }

  /**
   * Shows a help dialog for mapping a dice
   */
  showHelpMapping() {
    // TODO: show help
    throw new Error('Not implemented');
  }
}
