import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {forkJoin, catchError, of} from 'rxjs';
import {AuthService} from '../../services/auth.service';
import {EventService} from '../../services/event.service';
import {EventUtils} from '../../utils/event-utils';
import {Category} from '../../models/event.models';
import {PopupService} from "../../services/popup.service";

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent implements OnInit {
  events: any[] = [];
  isLoading = true;
  isLoggedIn = false;
  sectionTitle = 'Ближайшие мероприятия';
  isMobileMenuOpen = false;

  formatDate = EventUtils.formatDate;
  formatTime = EventUtils.formatTime;
  formatPrice = EventUtils.formatPrice;
  colorFromCode = EventUtils.colorFromCode;

  private categories: Category[] = [];

  constructor(
    private authService: AuthService,
    private eventService: EventService,
    private popupService: PopupService,
  ) {
  }

  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe(v => {
      this.isLoggedIn = v;
      this.updateSectionTitle();
    });

    forkJoin({
      categories: this.eventService.getCategories().pipe(catchError(() => of([]))),
      events: this.eventService.getRecommendedEvents(6).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({categories, events}) => {
        this.categories = categories;
        this.events = events.map((event: any) => ({
          id: event.idEvent,
          name: event.eventName,
          eventDate: event.eventDate,
          startTime: event.startTime,
          endTime: event.endTime,
          categoryName: event.eventCategoryName,
          categoryColor: this.getCategoryColor(event.idEventCategory),
          placeName: event.placeName,
          price: event.price,
          imageUrl: event.imageUrl,
          isOnline: event.eventFormat === 'ONLINE',
          maxParticipants: event.maxParticipants ?? null,
          currentParticipants: 0
        }));
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private getCategoryColor(categoryId: number): string {
    const cat = this.categories.find(c => c.idEventCategory === categoryId);
    return cat?.colorCode ?? '#9c89ff';
  }

  private updateSectionTitle(): void {
    if (this.isLoggedIn) {
      this.sectionTitle = 'Рекомендуем для вас';
    } else {
      this.sectionTitle = 'Ближайшие мероприятия';
    }
  }

  openLogin() {
    this.popupService.openLoginPopup();
    this.closeMobileMenu();
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
  }
}
