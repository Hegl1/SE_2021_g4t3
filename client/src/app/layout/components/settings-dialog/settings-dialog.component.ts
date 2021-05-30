import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { UserService } from 'src/app/core/auth/user.service';

const DEFAULT_PASSWORD = '123456789';
@Component({
  selector: 'tg-settings-dialog',
  templateUrl: './settings-dialog.component.html',
  styleUrls: ['./settings-dialog.component.scss'],
})
export class SettingsDialogComponent {
  settingsForm: FormGroup;

  private _editUsername = false;
  private _editPassword = false;

  private _saving = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private user: UserService,
    private dialogRef: MatDialogRef<SettingsDialogComponent>,
    private snackBar: MatSnackBar
  ) {
    this.settingsForm = this.fb.group(
      {
        username: [
          { value: this.user.user?.username || '', disabled: true },
          () => {
            if (this.editUsername && this.username?.value.trim().length === 0) {
              return { required: true };
            }
            return null;
          },
        ],
        current_password: ['', Validators.required],
        password: [
          { value: DEFAULT_PASSWORD, disabled: true },
          () => {
            if (this.editPassword && this.password?.value.trim().length === 0) {
              return { required: true };
            }
            return null;
          },
        ],
        password_confirm: [
          '',
          () => {
            if (this.editPassword && this.password?.value != this.password_confirm?.value) {
              return { passwordsMismatch: true };
            }
            if (this.editPassword && this.password_confirm?.value.trim().length === 0) {
              return { required: true };
            }
            return null;
          },
        ],
      },
      {
        validators: [
          () => {
            if (!this.editPassword && !this.editUsername) {
              return { updateRequired: true };
            }

            return null;
          },
        ],
      }
    );

    this.password?.valueChanges.subscribe(() => {
      if (this.password_confirm) {
        this.password_confirm.updateValueAndValidity();
      }
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

  get editUsername() {
    return this._editUsername;
  }
  set editUsername(editUsername: boolean) {
    this._editUsername = editUsername;

    if (this.username) {
      if (editUsername) {
        this.username.enable();
        this.username.setValue('');
      } else {
        this.username.disable();
        this.username.setValue(this.user.user?.username || '');
      }
    }
  }

  get editPassword() {
    return this._editPassword;
  }
  set editPassword(editPassword: boolean) {
    this._editPassword = editPassword;

    if (this.password) {
      if (editPassword) {
        this.password.enable();
        this.password.setValue('');
      } else {
        this.password.disable();
        this.password.setValue(DEFAULT_PASSWORD);
      }
    }
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

    if (this.editUsername) {
      updateValues.username = this.username?.value.trim();
    }
    if (this.editPassword) {
      updateValues.password = this.password?.value.trim();
    }

    let res = await this.api.updateUser(this.user.user.id, updateValues);

    this._saving = false;
    this.settingsForm.enable();
    this.dialogRef.disableClose = true;

    try {
      if (res.isOK()) {
        this.snackBar.open('Successfully updated settings!', 'OK', {
          duration: 5000,
        });

        this.dialogRef.close();

        await this.api.checkAuthentication();
      } else if (res.isConflict()) {
        throw new Error('This username is already taken!');
      } else if (res.isBadRequest()) {
        throw new Error('The entered password is wrong!');
      } else {
        throw new Error(res.error || undefined);
      }
    } catch (e) {
      this.error = e.message || 'An error occured!';
    }
  }

  public get saving() {
    return this._saving;
  }
}
