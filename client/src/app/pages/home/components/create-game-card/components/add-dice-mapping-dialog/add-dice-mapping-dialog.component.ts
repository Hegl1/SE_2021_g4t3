import { Component } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
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

  constructor(private dialogRef: MatDialogRef<AddDiceMappingDialogComponent>, private fb: FormBuilder) {
    this.mapping = this.fb.array([]);

    for (let i = 0; i < 12; i++) {
      this.mapping.push(
        this.fb.group({
          action: [null, Validators.required],
          time: [null, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]*$')]], // seconds
          points: [null, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]*$')]],
        })
      );
    }

    this.mappingForm = this.fb.group({
      name: ['', Validators.required],
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
