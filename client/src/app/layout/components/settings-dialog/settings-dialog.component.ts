import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'tg-settings-dialog',
  templateUrl: './settings-dialog.component.html',
  styleUrls: ['./settings-dialog.component.scss'],
})
export class SettingsDialogComponent {
  settingsForm: FormGroup;

  changePassword = false;
  private _saving = false;

  constructor(private fb: FormBuilder, private dialogRef: MatDialogRef<SettingsDialogComponent>) {
    this.settingsForm = this.fb.group({
      username: [''],
      current_password: ['', Validators.required],
      password: [''],
      password_confirm: [
        '',
        () => {
          if (this.password?.value != this.password_confirm?.value) {
            return { passwordsMismatch: true };
          }
          return null;
        },
      ],
    });
  }

  get username() {
    return this.settingsForm?.get('username');
  }
  get current_password() {
    return this.settingsForm?.get('current_password');
  }
  get password() {
    return this.settingsForm?.get('password');
  }
  get password_confirm() {
    return this.settingsForm?.get('password_confirm');
  }

  getFormError(key: string) {
    let field = this.settingsForm?.get(key);
    if (field?.invalid) {
      if (field.hasError('required')) {
        return 'This field is required';
      }
      if (field.hasError('passwordsMismatch')) {
        return "The passwords don't match";
      }
    }

    return null;
  }

  async save() {
    this._saving = true;
    this.settingsForm.disable();

    // TODO: save

    this._saving = false;
    this.settingsForm.enable();

    this.dialogRef.close();
  }

  public get saving() {
    return this._saving;
  }
}
