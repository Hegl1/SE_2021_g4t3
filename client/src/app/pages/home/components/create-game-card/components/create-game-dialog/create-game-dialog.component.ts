import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HelpDialogComponent } from 'src/app/components/help-dialog/help-dialog.component';
import { ApiService } from 'src/app/core/api/api.service';
import { Category, DiceMapping } from 'src/app/core/api/ApiInterfaces';
import StorageNames from 'src/app/core/StorageNames';
import { AddDiceMappingDialogComponent } from '../add-dice-mapping-dialog/add-dice-mapping-dialog.component';

@Component({
  selector: 'tg-create-game-dialog',
  templateUrl: './create-game-dialog.component.html',
  styleUrls: ['./create-game-dialog.component.scss'],
})
export class CreateGameDialogComponent implements OnInit {
  readonly DEFAULT_MAPPING_NAME = '__default_mapping__';

  loading_dice = false;
  loading_mapping = false;

  saving = false;
  error: string | null = null;

  categories: Category[] | null = null;

  createForm: FormGroup;
  diceMappings: { [key: string]: DiceMapping[] } | null = null;
  diceHistory: string[] | null = null;

  dice_icon: { color: 'primary' | 'accent' | 'warn' | null; value: string } = {
    color: null,
    value: 'vpn_key',
  };

  constructor(
    private api: ApiService,
    private dialogRef: MatDialogRef<CreateGameDialogComponent>,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    private dialog: MatDialog
  ) {
    this.createForm = this.fb.group({
      number_teams: [null, [Validators.required, Validators.min(2), Validators.pattern('^[0-9]*$')]],
      maximum_score: [null, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]*$')]],
      category: [{ value: null, disabled: true }, [Validators.required]],
      dice_code: [null, [Validators.required]],
      dice_mapping: [this.DEFAULT_MAPPING_NAME],
    });
  }

  async ngOnInit() {
    this.loadDiceHistory();

    let res = await this.api.getAllCategories();

    this.category?.enable();

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
  async checkDice() {
    this.loading_dice = true;
    this.dice_code?.disable();

    let res = await this.api.getDiceAvailable(this.dice_code?.value);

    this.loading_dice = false;
    this.dice_code?.enable();

    if (res.isOK()) {
      this.dice_icon = {
        color: 'primary',
        value: 'check',
      };
    } else {
      this.dice_icon.color = 'warn';

      let message: string | null = null;

      if (res.isNotFound()) {
        this.dice_icon.value = 'close';
        message = 'The dice was not found';
      } else if (res.isForbidden()) {
        this.dice_icon.value = 'not_interested';
        message = 'The dice is already in use';
      } else {
        this.dice_icon.value = 'error';
      }

      this.snackBar.open(message || res.error || 'Error loading dice information', 'OK', {
        duration: 5000,
        panelClass: 'action-warn',
      });
    }
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

    let mapping = null;

    if (this.dice_mapping?.value && this.dice_mapping?.value !== this.DEFAULT_MAPPING_NAME && this.diceMappings) {
      mapping = this.diceMappings[this.dice_mapping.value];
    }

    let res = await this.api.createGame(
      this.dice_code?.value,
      this.category?.value,
      this.maximum_score?.value,
      this.number_teams?.value,
      mapping
    );

    this.saving = false;
    this.createForm.enable();
    this.dialogRef.disableClose = false;

    if (res.isOK()) {
      this.saveDiceHistory();
      this.dialogRef.close(res.value);

      return;
    } else if (res.isNotFound()) {
      this.error = 'The selected dice does not exist';
    } else if (res.isConflict()) {
      this.error = 'The selected dice is already in a game';
    } else {
      this.error = res.error || 'Error creating game';
    }
  }

  /**
   * Loads a list of previously used time flips
   */
  loadDiceHistory() {
    this.diceHistory = JSON.parse(localStorage.getItem(StorageNames.DiceCodeHistory) || '[]');
  }

  /**
   * Saves the current time flip code to the history
   */
  saveDiceHistory() {
    let history = this.diceHistory;
    let value = this.dice_code?.value;

    if (value !== null && history !== null && !history.includes(value)) {
      history.push(value);
      localStorage.setItem(StorageNames.DiceCodeHistory, JSON.stringify(history));
    }
  }

  /**
   * Loads a list of saved time flip mappings
   */
  loadDiceMappings() {
    this.diceMappings = JSON.parse(localStorage.getItem(StorageNames.DiceMappings) || '{}');
  }

  get diceMappingsNames() {
    if (!this.diceMappings) return null;

    return Object.keys(this.diceMappings);
  }

  /**
   * Opens a dialog to create a new dice mapping
   * and stores it to localstorage
   */
  async addDiceMapping() {
    let res = await this.dialog.open(AddDiceMappingDialogComponent).afterClosed().toPromise();

    if (res) {
      this.loadDiceMappings();

      this.dice_mapping?.setValue(res);
    }
  }

  get dice_code() {
    return this.createForm.get('dice_code');
  }
  get category() {
    return this.createForm.get('category');
  }
  get maximum_score() {
    return this.createForm.get('maximum_score');
  }
  get number_teams() {
    return this.createForm.get('number_teams');
  }
  get dice_mapping() {
    return this.createForm.get('dice_mapping');
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
    this.dialog.open(HelpDialogComponent, {
      data: {
        key: 'DICE_CONNECTION',
      },
    });
  }

  /**
   * Shows a help dialog for mapping a dice
   */
  showHelpMapping() {
    this.dialog.open(HelpDialogComponent, {
      data: {
        key: 'DICE_MAPPING',
      },
    });
  }
}
