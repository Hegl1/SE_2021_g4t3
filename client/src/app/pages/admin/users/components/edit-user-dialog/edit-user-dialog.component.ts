import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { Role } from 'src/app/core/api/ApiInterfaces';
import { ApiResponse } from 'src/app/core/api/ApiResponse';
import { UserService } from 'src/app/core/auth/user.service';
import CustomValidators from 'src/app/core/forms/CustomValidators';

@Component({
  selector: 'tg-edit-user-dialog',
  templateUrl: './edit-user-dialog.component.html',
  styleUrls: ['./edit-user-dialog.component.scss'],
})
export class EditUserDialogComponent implements OnInit {
  userForm: FormGroup;

  changePassword = false;
  loading = false;
  saving = false;
  error: string | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      user_id: number | null;
    },
    private dialogRef: MatDialogRef<EditUserDialogComponent>,
    private fb: FormBuilder,
    private api: ApiService,
    private snackBar: MatSnackBar
  ) {
    this.userForm = this.fb.group({
      username: ['', Validators.required],
      role: ['', Validators.required],
      password: ['', CustomValidators.requiredIfTrue(() => this.changePassword || this.create)],
      password_confirm: [
        '',
        [
          CustomValidators.requiredIfTrue(() => this.changePassword || this.create),
          () => {
            if (this.password?.value != this.password_confirm?.value) {
              return { passwordsMismatch: true };
            }
            return null;
          },
        ],
      ],
    });
  }

  async ngOnInit() {
    if (this.data === null || this.data.user_id == null) return;

    this.loading = true;

    let res = await this.api.getUser(this.data.user_id);

    this.loading = false;

    if (!res.isOK()) {
      this.snackBar.open('Error loading user-information!', 'OK', {
        panelClass: 'action-warn',
        duration: 10000,
      });
      this.dialogRef.close(false);
    } else {
      if (res.value) {
        this.username?.setValue(res.value.username);
        this.role?.setValue(res.value.role);
      }
    }
  }

  get username() {
    return this.userForm?.get('username');
  }
  get role() {
    return this.userForm?.get('role');
  }
  get password() {
    return this.userForm?.get('password');
  }
  get password_confirm() {
    return this.userForm?.get('password_confirm');
  }

  getFormError(key: string) {
    let field = this.userForm?.get(key);
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

  getRoles(): string[] {
    return Object.values(Role);
  }

  /**
   * Save the updated user information
   */
  async save() {
    if (this.changePassword || this.create) {
      this.userForm.markAllAsTouched();
    } else {
      this.username?.markAsTouched();
      this.role?.markAsTouched();
    }

    this.userForm.updateValueAndValidity();

    let role: Role | null;

    if (this.userForm.invalid || (role = UserService.parseRole(this.role?.value)) === null) {
      return;
    }

    let res: ApiResponse<any>;

    this.saving = true;
    this.error = null;
    this.userForm.disable();
    this.dialogRef.disableClose = true;

    if (this.create) {
      res = await this.api.createUser(this.username?.value, this.password?.value, role);
    } else {
      let updateValues: any = {
        username: this.username?.value,
        role: this.role?.value,
      };

      if (this.changePassword && this.password?.value.trim().length > 0) {
        updateValues.password = this.password?.value.trim();
      }

      res = await this.api.updateUser(this.data.user_id || 0, updateValues);
    }

    this.saving = false;
    this.userForm.enable();
    this.dialogRef.disableClose = false;

    if (res.isOK()) {
      this.snackBar.open(`User was ${this.create ? 'created' : 'updated'} successfully!`, 'OK', {
        duration: 5000,
      });
      this.dialogRef.close(true);
    } else {
      if (res.isConflict()) {
        this.error = res.error || 'This username is already taken!';
      } else {
        this.error = res.error || 'Error saving user!';
      }
    }
  }

  get create() {
    return this.data === null || this.data?.user_id === null;
  }
}
