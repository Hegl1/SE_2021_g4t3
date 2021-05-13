import { Component, Inject } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DiceMapping } from 'src/app/core/api/ApiInterfaces';
import StorageNames from 'src/app/core/StorageNames';

@Component({
  selector: 'tg-add-dice-mapping-dialog',
  templateUrl: './add-dice-mapping-dialog.component.html',
  styleUrls: ['./add-dice-mapping-dialog.component.scss'],
})
export class AddDiceMappingDialogComponent {
  mappingForm: FormGroup;
  mapping: FormArray;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      edit: {
        name: string;
        data: DiceMapping[];
      } | null;
    },
    private dialogRef: MatDialogRef<AddDiceMappingDialogComponent>,
    private fb: FormBuilder
  ) {
    this.mapping = this.fb.array([]);

    for (let i = 0; i < 12; i++) {
      let values: DiceMapping | null = null;

      if (this.data && this.data.edit) {
        values = {
          action: this.data.edit?.data[i].action,
          time: this.data.edit?.data[i].time,
          points: this.data.edit?.data[i].points,
        };
      }

      this.mapping.push(
        this.fb.group({
          action: [values?.action || null, Validators.required],
          time: [values?.time || null, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]*$')]], // seconds
          points: [values?.points || null, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]*$')]],
        })
      );
    }

    this.mappingForm = this.fb.group({
      name: [
        { value: this.data && this.data.edit ? this.data.edit.name : '', disabled: this.data && !!this.data.edit },
        Validators.required,
      ],
      mapping: this.mapping,
    });
  }

  getFormError(key: string, index: number | null = null, name: string | null = null) {
    let field: AbstractControl | null;

    if (index === null || name === null) {
      field = this.mappingForm?.get(key);
    } else {
      field = this.mapping.controls[index].get(name);
    }

    if (field?.invalid) {
      if (field.hasError('required')) {
        return 'This field is required';
      }
      if (field.hasError('min')) {
        return 'The number is too small';
      }
      if (field.hasError('pattern')) {
        return 'A positive integer must be supplied';
      }
    }

    return null;
  }

  get valid() {
    return this.mappingForm.valid;
  }

  /**
   * Saves the created mapping and returns the name
   */
  save() {
    let name = this.mappingForm.get('name');

    if (!this.valid || !name) return;

    let diceMappings: { [key: string]: DiceMapping[] } = JSON.parse(
      localStorage.getItem(StorageNames.DiceMappings) || '{}'
    );

    let mapping: DiceMapping[] = [];

    for (let i = 0; i < 12; i++) {
      mapping.push({
        action: this.mapping.controls[i].get('action')?.value || '',
        points: parseInt(this.mapping.controls[i].get('points')?.value || 0),
        time: parseInt(this.mapping.controls[i].get('time')?.value || 0),
      });
    }

    diceMappings[name.value] = mapping;

    localStorage.setItem(StorageNames.DiceMappings, JSON.stringify(diceMappings));

    this.dialogRef.close(name.value);
  }
}
