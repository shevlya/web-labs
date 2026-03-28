import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {ERROR_MESSAGES} from '../../constants/errors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage = '';
  isLoading = false;

  get usernameControl() {
    return this.loginForm.get('username');
  }

  get passwordControl() {
    return this.loginForm.get('password');
  }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });

    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/tasks']);
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.setLoading(true);
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.setLoading(false);
        this.router.navigate(['/tasks']);
      },
      error: (err: HttpErrorResponse) => {
        this.setLoading(false);
        this.errorMessage = (err.status === 401 || err.status === 403) ? ERROR_MESSAGES.AUTH.INVALID_CREDENTIALS : ERROR_MESSAGES.AUTH.SERVER_ERROR;
      }
    });
  }

  private setLoading(loading: boolean): void {
    this.isLoading = loading;
    if (loading) this.errorMessage = '';
  }
}
