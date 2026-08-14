import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

import { AuthService } from '../../services/auth.service';
import { EventUtils } from '../../utils/event-utils';
import { environment } from '../../environment';
import {Avatar, CategoryDto, OrganizerRequest, User, UserInterestDto} from "../../models/user.models";

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.scss']
})
export class ProfilePageComponent implements OnInit, OnDestroy {
  private readonly API = environment.apiUrl;
  private userSub!: Subscription;

  protected readonly EventUtils = EventUtils;
  readonly today = new Date().toISOString().split('T')[0];

  currentUser: User | null = null;
  userBirthDate: string | null = null;
  hasDisability = false;
  disabilityUpdating = false;

  favoriteCategories: { id: number; name: string; color: string }[] = [];
  allCategories: CategoryDto[] = [];
  selectedCategoryIds: Set<number> = new Set();
  readonly maxVisibleCategories = 5;
  dropdownOpen = false;
  showCategoriesModal = false;
  categoriesLoading = false;
  categoriesSaving = false;
  categoriesError: string | null = null;

  organizerRequest: OrganizerRequest | null = null;
  organizerForm!: FormGroup;
  showOrganizerModal = false;
  organizerLoading = false;
  organizerError: string | null = null;
  organizerSuccess = false;

  avatarList: Avatar[] = [];
  showAvatarPicker = false;
  avatarSaving = false;
  avatarSaveError = false;

  passwordForm!: FormGroup;
  showPasswordModal = false;
  passwordLoading = false;
  passwordError: string | null = null;
  passwordSuccess = false;

  birthdayForm!: FormGroup;
  showBirthdayModal = false;
  birthdayLoading = false;
  birthdayError: string | null = null;
  birthdaySuccess = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForms();

    this.userSub = this.authService.user$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.hasDisability = user.hasDisability ?? false;
        this.userBirthDate = user.birthDate ? EventUtils.formatDate(user.birthDate) : null;

        if (this.authService.getToken()) {
          this.loadProfileData();
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }

  private initForms(): void {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    this.birthdayForm = this.fb.group({
      birthDate: ['', Validators.required]
    });

    this.organizerForm = this.fb.group({
      requestText: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  private loadProfileData(): void {
    this.loadInterests();
    this.loadAvatars();

    if (this.userRole !== 'ADMIN') {
      this.loadOrganizerRequest();
    } else {
      this.organizerRequest = null;
    }
  }

  private loadInterests(): void {
    this.http.get<UserInterestDto[]>(`${this.API}/users/me/interests`).pipe(
      catchError(err => {
        console.error('Ошибка загрузки интересов:', err);
        return of([]);
      })
    ).subscribe(interests => {
      this.favoriteCategories = interests.map(i => ({
        id: i.id,
        name: i.name,
        color: EventUtils.colorFromCode(i.colorCode)
      }));
    });
  }

  private loadAvatars(): void {
    this.http.get<Avatar[]>(`${this.API}/avatars`).pipe(
      catchError(err => {
        console.error('Ошибка загрузки аватаров:', err);
        return of([]);
      })
    ).subscribe(avatars => this.avatarList = avatars);
  }

  private loadOrganizerRequest(): void {
    this.http.get<OrganizerRequest>(`${this.API}/organizer-requests/me`).pipe(
      catchError(err => {
        if (err.status !== 404) console.error('Ошибка загрузки заявки:', err);
        return of(null);
      })
    ).subscribe(req => {
      if (req) this.organizerRequest = req;
    });
  }

  get userName(): string { return this.currentUser?.fio ?? ''; }
  get userEmail(): string { return this.currentUser?.email ?? ''; }
  get userAvatar(): string { return this.currentUser?.avatarUrl ?? 'assets/avatars/avatar0.jpg'; }
  get userRole(): string { return this.currentUser?.roleName ?? ''; }

  get visibleCategories() { return this.favoriteCategories.slice(0, this.maxVisibleCategories); }
  get hiddenCategories() { return this.favoriteCategories.slice(this.maxVisibleCategories); }
  get hasMoreCategories() { return this.favoriteCategories.length > this.maxVisibleCategories; }
  get hiddenCategoriesCount() { return this.favoriteCategories.length - this.maxVisibleCategories; }

  toggleDisability(): void {
    this.disabilityUpdating = true;
    this.authService.updateDisability(!this.hasDisability).subscribe({
      next: (user) => {
        this.hasDisability = user.hasDisability ?? false;
        this.disabilityUpdating = false;
      },
      error: () => this.disabilityUpdating = false
    });
  }

  openAvatarPicker(): void {
    this.avatarSaveError = false;
    setTimeout(() => this.showAvatarPicker = true, 0);
  }

  closeAvatarPicker(): void {
    this.showAvatarPicker = false;
  }

  isSelectedAvatar(avatarId: number): boolean {
    return this.currentUser?.avatarId === avatarId;
  }

  selectAvatar(avatarId: number): void {
    this.avatarSaving = true;
    this.authService.updateProfile({ idAvatar: avatarId }).subscribe({
      next: () => {
        this.avatarSaving = false;
        this.showAvatarPicker = false;
        this.authService.refreshUserFromServer().subscribe();
      },
      error: () => {
        this.avatarSaving = false;
        this.avatarSaveError = true;
      }
    });
  }

  openPasswordModal(): void {
    this.passwordForm.reset();
    this.passwordError = null;
    this.passwordSuccess = false;
    this.showPasswordModal = true;
  }

  closePasswordModal(): void {
    if (!this.passwordLoading) this.showPasswordModal = false;
  }

  submitPasswordChange(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.passwordLoading = true;
    this.passwordError = null;
    const { currentPassword, newPassword } = this.passwordForm.value;

    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.passwordLoading = false;
        this.passwordSuccess = true;
        setTimeout(() => {
          this.showPasswordModal = false;
          this.passwordSuccess = false;
        }, 1500);
      },
      error: (err) => {
        this.passwordLoading = false;
        this.passwordError = err.error?.message ?? 'Ошибка при смене пароля';
      }
    });
  }

  private passwordMatchValidator(control: AbstractControl) {
    const pw = control.get('newPassword');
    const cpw = control.get('confirmPassword');
    if (pw && cpw && pw.value !== cpw.value) {
      cpw.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  showFieldError(field: string): boolean {
    const c = this.passwordForm.get(field);
    return !!c && c.invalid && c.touched;
  }

  openBirthdayModal(): void {
    this.birthdayForm.setValue({ birthDate: this.currentUser?.birthDate?.split('T')[0] || '' });
    this.birthdayError = null;
    this.birthdaySuccess = false;
    this.showBirthdayModal = true;
  }

  closeBirthdayModal(): void {
    if (!this.birthdayLoading) this.showBirthdayModal = false;
  }

  submitBirthday(): void {
    if (this.birthdayForm.invalid) {
      this.birthdayForm.markAllAsTouched();
      return;
    }

    this.birthdayLoading = true;
    this.birthdayError = null;

    this.authService.updateProfile({ birthDate: this.birthdayForm.value.birthDate }).subscribe({
      next: (user) => {
        this.birthdayLoading = false;
        this.birthdaySuccess = true;
        this.userBirthDate = user.birthDate ? EventUtils.formatDate(user.birthDate) : null;
        setTimeout(() => {
          this.showBirthdayModal = false;
          this.birthdaySuccess = false;
        }, 1500);
      },
      error: (err) => {
        this.birthdayLoading = false;
        this.birthdayError = err.error?.message ?? 'Ошибка при сохранении';
      }
    });
  }

  showBirthdayError(field: string): boolean {
    const c = this.birthdayForm.get(field);
    return !!c && c.invalid && c.touched;
  }

  openCategoriesModal(): void {
    this.categoriesError = null;
    this.selectedCategoryIds = new Set(this.favoriteCategories.map(c => c.id));
    this.showCategoriesModal = true;

    if (this.allCategories.length === 0) {
      this.categoriesLoading = true;
      this.http.get<CategoryDto[]>(`${this.API}/categories`).pipe(
        catchError(err => {
          console.error('Ошибка загрузки категорий:', err);
          this.categoriesError = 'Не удалось загрузить категории';
          return of([]);
        })
      ).subscribe(cats => {
        this.allCategories = cats;
        this.categoriesLoading = false;
      });
    }
  }

  closeCategoriesModal(): void {
    if (!this.categoriesSaving) this.showCategoriesModal = false;
  }

  toggleCategory(id: number): void {
    if (this.selectedCategoryIds.has(id)) {
      this.selectedCategoryIds.delete(id);
    } else {
      if (this.selectedCategoryIds.size < 5) {
        this.selectedCategoryIds.add(id);
      } else {
        this.categoriesError = 'Нельзя выбрать более 5 категорий';
        return;
      }
    }
    this.categoriesError = null;
  }

  isCategorySelected(id: number): boolean {
    return this.selectedCategoryIds.has(id);
  }

  submitCategories(): void {
    const categoryIds = Array.from(this.selectedCategoryIds).filter(id => id != null && !isNaN(Number(id)));

    if (categoryIds.length === 0) {
      this.categoriesError = 'Выберите хотя бы одну категорию';
      return;
    }
    if (categoryIds.length > 5) {
      this.categoriesError = 'Нельзя выбрать более 5 категорий';
      return;
    }

    this.categoriesSaving = true;
    this.categoriesError = null;

    this.http.put<any>(`${this.API}/users/me/interests`, { categoryIds }).pipe(
      catchError(err => {
        this.categoriesSaving = false;
        this.categoriesError = err.error?.message || 'Ошибка при сохранении категорий';
        return of(null);
      })
    ).subscribe(result => {
      if (result === null) return;
      this.loadInterests();
      this.categoriesSaving = false;
      this.showCategoriesModal = false;
    });
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  openOrganizerModal(): void {
    this.organizerForm.reset();
    this.organizerError = null;
    this.organizerSuccess = false;
    this.showOrganizerModal = true;
  }

  closeOrganizerModal(): void {
    if (!this.organizerLoading) this.showOrganizerModal = false;
  }

  submitOrganizerRequest(): void {
    if (this.organizerForm.invalid) {
      this.organizerForm.markAllAsTouched();
      return;
    }

    this.organizerLoading = true;
    this.organizerError = null;

    this.http.post(`${this.API}/organizer-requests/me`, { requestText: this.organizerForm.value.requestText }).pipe(
      catchError(err => {
        this.organizerLoading = false;
        this.organizerError = err.error?.message ?? 'Ошибка при отправке заявки';
        return of(null);
      })
    ).subscribe((result) => {
      if (result === null) return;

      this.organizerLoading = false;
      this.organizerSuccess = true;
      this.loadOrganizerRequest();

      setTimeout(() => {
        this.showOrganizerModal = false;
        this.organizerSuccess = false;
      }, 1500);
    });
  }

  showOrganizerError(field: string): boolean {
    const c = this.organizerForm.get(field);
    return !!c && c.invalid && c.touched;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const t = event.target as HTMLElement;
    if (!t.closest('.dropdown-container')) {
      this.dropdownOpen = false;
    }
    if (this.showAvatarPicker && !t.closest('.avatar-picker-modal') && !t.closest('.avatar-change-btn')) {
      this.showAvatarPicker = false;
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
