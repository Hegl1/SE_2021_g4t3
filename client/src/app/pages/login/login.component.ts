import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'tg-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  isLogin = this.router.url === '/login';

  loginForm: FormGroup;

  constructor(private router: Router, private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      password_confirm: [
        '',
        () => {
          if (this.password?.value != this.password_confirm?.value) {
            return { passwordsMismatch: true };
          }
          return null;
        },
      ],
      remember: [false],
    });
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
  action() {
    // TODO: implement
  }
}
