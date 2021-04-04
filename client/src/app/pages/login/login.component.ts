import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from 'src/app/core/api/api.service';
import { User } from 'src/app/core/api/ApiInterfaces';
import { ApiResponse } from 'src/app/core/api/ApiResponse';
import { UserService } from 'src/app/core/auth/user.service';

@Component({
  selector: 'tg-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  isLogin = this.router.url.startsWith('/login');

  loginForm: FormGroup;

  error: string | null = null;
  loading = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private api: ApiService,
    private user: UserService,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      password_confirm: [
        '',
        () => {
          if (!this.isLogin && this.password?.value != this.password_confirm?.value) {
            return { passwordsMismatch: true };
          }
          return null;
        },
      ],
      remember: [false],
    });

    this.route.queryParams.subscribe((params) => {
      if (params.reason) {
        let reason: string;

        switch (params.reason) {
          case 'logout':
            reason = 'Successfully logged out!';
            break;
          case 'badToken':
          case 'unauthorized':
            reason = 'You have to log back in!';
            break;
          default:
            reason = 'You have been logged out';
        }

        this.snackBar.open(reason, 'OK', {
          duration: 5000,
        });
      }
    });
  }

  ngOnInit() {
    if (this.user.isLoggedin) {
      this.router.navigateByUrl(this.route.snapshot.queryParams.redirectUrl || '/');
    }
  }

  get username() {
    return this.loginForm?.get('username');
  }
  get password() {
    return this.loginForm?.get('password');
  }
  get password_confirm() {
    return this.loginForm?.get('password_confirm');
  }
  get remember() {
    return this.loginForm?.get('remember');
  }

  getFormError(key: string) {
    let field = this.loginForm?.get(key);
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
   * Login/Register
   */
  async action() {
    if (this.loginForm.invalid) return;

    this.error = null;
    this.loading = true;
    this.loginForm.disable();

    let res: ApiResponse<{ user: User; token: string }>;

    if (this.isLogin) {
      res = await this.api.loginUser(this.username?.value, this.password?.value);
    } else {
      res = await this.api.registerUser(this.username?.value, this.password?.value);
    }

    this.loading = false;
    this.loginForm.enable();

    if (res.isOK() && res.value !== null) {
      this.user.login(res.value.token, this.remember?.value);

      this.router.navigateByUrl(this.route.snapshot.queryParams.redirectUrl || '/');
    } else if (res.isUnauthorized()) {
      this.error = 'Username or password wrong!';
    } else if (res.isConflict()) {
      this.error = 'This username is already taken!';
    } else {
      this.error = 'An error occured!';
    }
  }
}
