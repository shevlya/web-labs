import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { LoginPopupComponent } from './pages/login-popup/login-popup.component';
import { PopupService } from './services/popup.service';
import { Subscription } from 'rxjs';
import { HeaderComponent } from './components/header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, LoginPopupComponent, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  showLoginPopup = false;
  private subscription!: Subscription;

  constructor(private popupService: PopupService) {}

  ngOnInit() {
    this.subscription = this.popupService.showLoginPopup$.subscribe(show => {
      this.showLoginPopup = show;
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
