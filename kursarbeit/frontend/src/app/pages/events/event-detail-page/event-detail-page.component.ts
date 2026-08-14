import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, ActivatedRoute, Router} from '@angular/router';
import {forkJoin, lastValueFrom} from 'rxjs';
import {EventDetail, OnlinePlaceCreateDto, PhysicalPlaceCreateDto} from "../../../models/event.models";
import {EventUtils} from "../../../utils/event-utils";
import {EventService} from "../../../services/event.service";
import {AuthService} from "../../../services/auth.service";
import {PopupService} from "../../../services/popup.service";

@Component({
  selector: 'app-event-detail-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './event-detail-page.component.html',
  styleUrls: ['./event-detail-page.component.scss']
})
export class EventDetailPageComponent implements OnInit {
  event: EventDetail | null = null;
  isLoading = true;
  isLoggedIn = false;

  physicalPlace: PhysicalPlaceCreateDto | null = null;
  onlinePlace: OnlinePlaceCreateDto | null = null;

  participantsCount = 0;
  isRegistered = false;
  registrationLoading = false;
  registrationError: string | null = null;
  registrationSuccess: string | null = null;

  formatDate = EventUtils.formatDate;
  formatTime = EventUtils.formatTime;
  formatPrice = EventUtils.formatPrice;
  colorFromCode = EventUtils.colorFromCode;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private authService: AuthService,
    private popupService: PopupService
  ) {
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(id)) {
      this.router.navigate(['/events']);
      return;
    }
    this.isLoggedIn = this.authService.getCurrentUser() !== null;
    forkJoin({
      categories: this.eventService.getCategories(),
      event: this.eventService.getEventById(id)
    }).subscribe({
      next: async (res) => {
        const cat = res.categories.find(c => c.idEventCategory === res.event.idEventCategory);
        this.event = {...res.event, categoryColor: cat?.colorCode || '#9c89ff'};

        if (this.event.idPlace) {
          try {
            const type = this.event.placeType?.trim().toUpperCase();
            if (type === 'ONLINE') {
              this.onlinePlace = await lastValueFrom(this.eventService.getOnlinePlace(this.event.idPlace));
              this.physicalPlace = null;
            } else {
              this.physicalPlace = await lastValueFrom(this.eventService.getPhysicalPlace(this.event.idPlace));
              this.onlinePlace = null;
            }
          } catch (e) {
            console.error('Ошибка загрузки места:', e);
          }
        }

        this.loadParticipantsCount();
        if (this.isLoggedIn) {
          this.loadRegistrationStatus();
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.router.navigate(['/events']);
      }
    });
  }

  private loadParticipantsCount(): void {
    if (!this.event) return;
    this.eventService.getRegisteredParticipantsCount(this.event.idEvent).subscribe(c => this.participantsCount = c);
  }

  private loadRegistrationStatus(): void {
    if (!this.event) return;
    this.eventService.isUserRegistered(this.event.idEvent).subscribe(r => this.isRegistered = r);
  }

  get isRegistrationOpen(): boolean {
    return !this.event?.maxParticipants || this.participantsCount < this.event.maxParticipants;
  }

  register(): void {
    if (!this.isLoggedIn) {
      this.popupService.openLoginPopup();
      return;
    }
    if (!this.event) return;

    this.registrationLoading = true;
    this.registrationError = null;

    const action$ = this.isRegistered
      ? this.eventService.cancelRegistration(this.event.idEvent)
      : this.eventService.registerForEvent(this.event.idEvent);

    action$.subscribe({
      next: () => {
        this.registrationSuccess = this.isRegistered ? 'Запись отменена' : 'Вы записаны на мероприятие!';
        this.participantsCount += this.isRegistered ? -1 : 1;
        this.isRegistered = !this.isRegistered;
        this.registrationLoading = false;
        setTimeout(() => this.registrationSuccess = null, 3000);
      },
      error: (err) => {
        this.registrationError = err.error?.message || 'Произошла ошибка';
        this.registrationLoading = false;
        setTimeout(() => this.registrationError = null, 4000);
      }
    });
  }
}
