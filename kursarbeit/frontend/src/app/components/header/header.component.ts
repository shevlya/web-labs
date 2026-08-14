import {Component, OnInit, HostListener, inject, DestroyRef} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {NgTemplateOutlet} from '@angular/common';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {PopupService} from '../../services/popup.service';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, NgTemplateOutlet],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  private authService = inject(AuthService);
  private popupService = inject(PopupService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  isLoggedIn = false;
  userName = '';
  userAvatar = '';
  userRole = '';
  isProfileMenuOpen = false;
  isMobileMenuOpen = false;

  readonly defaultAvatar = 'assets/avatars/avatar0.jpg';

  ngOnInit() {
    this.authService.isLoggedIn$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => this.isLoggedIn = value);

    this.authService.user$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        if (user) {
          this.userName = user.fio;
          this.userRole = user.roleName ?? '';
          this.userAvatar = (user.avatarUrl?.trim() && user.avatarUrl !== 'null')
            ? user.avatarUrl
            : this.defaultAvatar;
        } else {
          this.userName = '';
          this.userAvatar = this.defaultAvatar;
          this.userRole = '';
        }
      });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;

    if (!target.closest('.user-profile') && this.isProfileMenuOpen) {
      this.isProfileMenuOpen = false;
    }

    if (!target.closest('.mobile-menu-btn') &&
      !target.closest('.main-nav') &&
      this.isMobileMenuOpen) {
      this.isMobileMenuOpen = false;
    }
  }

  get isOrganizer(): boolean {
    return this.userRole === 'ORGANIZER';
  }

  get isAdmin(): boolean {
    return this.userRole === 'ADMIN';
  }

  toggleProfileMenu() {
    this.isProfileMenuOpen = !this.isProfileMenuOpen;
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
    if (this.isMobileMenuOpen) this.isProfileMenuOpen = false;
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
  }

  private navigateWithMenuClose(path: string) {
    this.isProfileMenuOpen = false;
    this.closeMobileMenu();
    this.router.navigate([path]);
  }

  openLogin() {
    this.popupService.openLoginPopup();
    this.closeMobileMenu();
  }

  logout() {
    this.authService.logout();
    this.isProfileMenuOpen = false;
    this.navigateWithMenuClose('/');
  }

  openProfile() {
    this.navigateWithMenuClose('/profile');
  }

  openMyEvents() {
    this.navigateWithMenuClose('/my-events');
  }

  openCreateEvent() {
    this.navigateWithMenuClose('/events/create');
  }

  openManageEvents() {
    this.navigateWithMenuClose('/events/manage');
  }

  openAdminPanel() {
    this.navigateWithMenuClose('/admin');
  }
}
