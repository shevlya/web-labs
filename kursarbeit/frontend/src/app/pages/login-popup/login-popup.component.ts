import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl} from '@angular/forms';
import {PopupService} from '../../services/popup.service';
import {AuthService} from '../../services/auth.service';
import {OrganizerRequestService} from '../../services/organizer-request.service';

@Component({
  selector: 'app-login-popup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-popup.component.html',
  styleUrls: ['./login-popup.component.scss']
})
export class LoginPopupComponent implements OnInit {
  loginForm!: FormGroup;
  registerForm!: FormGroup;
  isLoading = false;
  submitted = false;
  showRegisterForm = false;
  serverError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private popupService: PopupService,
    private authService: AuthService,
    private organizerRequestService: OrganizerRequestService) {
  }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });

    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.pattern(/^[a-zA-Zа-яА-ЯёЁ]+$/)]],
      lastName: ['', [Validators.required, Validators.pattern(/^[a-zA-Zа-яА-ЯёЁ]+$/)]],
      email: ['', [Validators.required, Validators.email, this.emailStartValidator]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      hasDisability: [false],
      wantsOrganizer: [false],
      organizerRequestText: ['']
    }, {validators: this.passwordMatchValidator});
  }

  onWantsOrganizerChange(): void {
    const ctrl = this.registerForm.get('organizerRequestText');
    if (this.registerForm.get('wantsOrganizer')?.value) {
      ctrl?.setValidators([Validators.required]);
    } else {
      ctrl?.clearValidators();
      ctrl?.setValue('');
    }
    ctrl?.updateValueAndValidity();
  }

  passwordMatchValidator(control: AbstractControl) {
    const pw = control.get('password')?.value;
    const cpw = control.get('confirmPassword')?.value;
    return pw === cpw ? null : {passwordMismatch: true};
  }

  emailStartValidator(control: AbstractControl) {
    const v = control.value;
    return (v && !/^[a-zA-Zа-яА-Я]/.test(v.charAt(0))) ? {emailStart: true} : null;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    this.serverError = null;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => this.popupService.closeLoginPopup(),
      error: (err) => {
        this.isLoading = false;
        this.serverError = err.status === 401 ? 'Неверный email или пароль' : 'Ошибка сервера';
      }
    });
  }

  onRegister(): void {
    this.submitted = true;
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.serverError = null;
    const f = this.registerForm.value;

    this.authService.register({
      fio: `${f.firstName} ${f.lastName}`,
      email: f.email,
      password: f.password,
      hasDisability: f.hasDisability
    }).subscribe({
      next: () => {
        if (f.wantsOrganizer && f.organizerRequestText) {
          this.organizerRequestService.createRequest(f.organizerRequestText).subscribe();
        }
        this.popupService.closeLoginPopup();
      },
      error: (err) => {
        this.isLoading = false;
        this.serverError = err.status === 409 ? 'Email уже занят' : 'Ошибка регистрации';
      }
    });
  }

  toggleForm(): void {
    this.showRegisterForm = !this.showRegisterForm;
    this.submitted = false;
    this.serverError = null;
  }

  closePopup(): void {
    this.popupService.closeLoginPopup();
  }

  showRegisterFieldError(field: string): boolean {
    const c = this.registerForm.get(field);
    return !!c && c.invalid && (c.touched || this.submitted);
  }

  showLoginEmailError(): boolean {
    const c = this.loginForm.get('email');
    return !!c && c.invalid && (c.touched || this.submitted);
  }

  showLoginPasswordError(): boolean {
    const c = this.loginForm.get('password');
    return !!c && c.invalid && (c.touched || this.submitted);
  }
}
