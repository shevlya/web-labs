import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PopupService {
  private showLoginPopupSource = new Subject<boolean>();
  showLoginPopup$ = this.showLoginPopupSource.asObservable();

  openLoginPopup() {
    this.showLoginPopupSource.next(true);
  }

  closeLoginPopup() {
    this.showLoginPopupSource.next(false);
  }
}
