import {Component, OnInit, computed, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Router} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {AuthService} from '../../../services/auth.service';
import {EventUtils} from '../../../utils/event-utils';
import {ConfirmationDialogComponent} from "../../../components/confirmation-dialog/confirmation-dialog.component";
import {EventParticipant} from '../../../models/event.models';

@Component({
  selector: 'app-my-events',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmationDialogComponent],
  templateUrl: './my-events.component.html',
  styleUrls: ['./my-events.component.scss']
})
export class MyEventsComponent implements OnInit {
  participations = signal<EventParticipant[]>([]);
  selectedStatusFilter = signal<string>('ALL');

  isLoading = true;
  error = signal<string | null>(null);
  cancelLoading = false;

  showCancelDialog = false;
  cancelEventId: number | null = null;
  cancelEventName: string = '';
  cancelMessage: string = '';

  formatDate = EventUtils.formatDate;

  filteredParticipations = computed(() => {
    const filter = this.selectedStatusFilter();
    const all = this.participations();
    if (filter === 'ALL') return all;
    return all.filter(p => p.participationStatus === filter);
  });

  getCounts(status: string): number {
    if (status === 'ALL') return this.participations().length;
    return this.participations().filter(p => p.participationStatus === status).length;
  }

  constructor(
    private eventService: EventService,
    private authService: AuthService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.loadParticipations();
  }

  private loadParticipations(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.error.set('Пользователь не авторизован');
      this.isLoading = false;
      return;
    }

    this.eventService.getUserParticipations(currentUser.userId).subscribe({
      next: (data) => {
        this.participations.set(data);
        this.isLoading = false;
      },
      error: (err) => {
        this.error.set('Ошибка загрузки ваших мероприятий');
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  setFilter(status: string): void {
    this.selectedStatusFilter.set(status);
  }

  openCancelDialog(eventId: number, eventName: string): void {
    this.cancelEventId = eventId;
    this.cancelEventName = eventName;
    this.cancelMessage = `Вы действительно хотите отменить запись на мероприятие "${eventName}"?`;
    this.showCancelDialog = true;
  }

  confirmCancel(): void {
    if (this.cancelEventId === null) return;
    this.cancelLoading = true;
    this.eventService.cancelRegistration(this.cancelEventId).subscribe({
      next: () => {
        this.loadParticipations();
        this.cancelLoading = false;
        this.closeCancelDialog();
      },
      error: () => {
        this.cancelLoading = false;
        this.closeCancelDialog();
        alert('Не удалось отменить запись');
      }
    });
  }

  closeCancelDialog(): void {
    this.showCancelDialog = false;
    this.cancelEventId = null;
  }

  viewEvent(eventId: number): void {
    this.router.navigate(['/events', eventId]);
  }

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      'REGISTERED': 'status-registered',
      'WAITLISTED': 'status-waitlist',
      'ATTENDED': 'status-attended',
      'CANCELLED': 'status-cancelled',
      'REJECTED_BY_ORGANIZER': 'status-rejected'
    };
    return classes[status] || '';
  }

  getStatusText(status: string): string {
    const texts: Record<string, string> = {
      'REGISTERED': 'Записан',
      'WAITLISTED': 'Лист ожидания',
      'ATTENDED': 'Посещено',
      'CANCELLED': 'Отменено',
      'REJECTED_BY_ORGANIZER': 'Отклонено'
    };
    return texts[status] || status;
  }
}
