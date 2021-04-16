import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { UserService } from 'src/app/core/auth/user.service';

@Component({
  selector: 'tg-settings-dialog',
  templateUrl: './settings-dialog.component.html',
  styleUrls: ['./settings-dialog.component.scss'],
})
export class SettingsDialogComponent {
  settingsForm: FormGroup;

  changePassword = false;
  private _saving = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private user: UserService,
    private dialogRef: MatDialogRef<SettingsDialogComponent>,
    private snackBar: MatSnackBar
  ) {
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

  /**
   * Save the updated user information
   */
  async save() {
    this.settingsForm.markAllAsTouched();

    if (!this.user.user || this.settingsForm.invalid) {
      return;
    }

    this._saving = true;
    this.settingsForm.disable();
    this.error = null;
    this.dialogRef.disableClose = true;

    let updateValues: any = {
      old_password: this.current_password?.value,
    };

    if (this.username?.value.trim().length > 0) {
      updateValues.username = this.username?.value.trim();
    }
    if (this.changePassword && this.password?.value.trim().length > 0) {
      updateValues.password = this.password?.value.trim();
    }

    let res = await this.api.updateUser(this.user.user.id, updateValues);

    this._saving = false;
    this.settingsForm.enable();
    this.dialogRef.disableClose = true;

    try {
      if (res.isOK()) {
        // TODO: check auth/logout
        this.snackBar.open('Successfully updated settings!', 'OK', {
          duration: 5000,
        });

        this.dialogRef.close();
      } else if (res.isConflict()) {
        throw new Error('This username is already taken!');
      } else {
        throw new Error();
      }
    } catch (e) {
      this.error = e.message || 'An error occured!';
    }
  }

  public get saving() {
    return this._saving;
  }
}
