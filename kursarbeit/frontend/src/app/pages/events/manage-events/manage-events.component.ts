import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Router} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {AuthService} from '../../../services/auth.service';
import {EventCard} from '../../../models/event.models';
import {EventUtils} from '../../../utils/event-utils';
import {ParticipantsModalComponent} from "../../../components/participants-modal/participants-modal.component";
import {ConfirmationDialogComponent} from "../../../components/confirmation-dialog/confirmation-dialog.component";

@Component({
  selector: 'app-manage-events',
  standalone: true,
  imports: [CommonModule, RouterModule, ParticipantsModalComponent, ConfirmationDialogComponent],
  templateUrl: './manage-events.component.html',
  styleUrls: ['./manage-events.component.scss']
})
export class ManageEventsComponent implements OnInit {
  events: EventCard[] = [];
  activeEvents: EventCard[] = [];
  archivedEvents: EventCard[] = [];
  isLoading = true;
  error: string | null = null;
  deleteLoading = false;

  showParticipantsModal = false;
  showDeleteDialog = false;

  selectedEventForParticipants: { id: number; name: string } | null = null;
  eventIdToDelete: number | null = null; // ID мероприятия для удаления

  displayType: 'active' | 'archived' = 'active';

  formatDate = EventUtils.formatDate;
  formatTime = EventUtils.formatTime;
  colorFromCode = EventUtils.colorFromCode;

  constructor(
    private eventService: EventService,
    private authService: AuthService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.loadEvents();
  }

  private loadEvents(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.error = 'Пользователь не авторизован';
      this.isLoading = false;
      return;
    }

    this.eventService.getEventsByOrganizer(currentUser.userId).subscribe({
      next: (events) => {
        this.events = events.map(e => ({
          ...e,
          isOnline: e.eventFormat === 'ONLINE' || e.placeType === 'ONLINE',
          categoryColor: e.categoryColor || '#9c89ff'
        }));
        this.filterEvents();
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Ошибка загрузки мероприятий';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  private filterEvents(): void {
    const now = new Date();
    this.activeEvents = this.events.filter(event => new Date(event.endTime) >= now);
    this.archivedEvents = this.events.filter(event => new Date(event.endTime) < now);
  }

  setDisplayType(type: 'active' | 'archived'): void {
    this.displayType = type;
  }

  getModerationStatusText(status: string | undefined): string {
    if (!status) return 'Черновик';
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 'На модерации';
      case 'PUBLISHED':
        return 'Опубликовано';
      case 'REJECTED':
        return 'Отклонено';
      default:
        return 'Черновик';
    }
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'bg-light text-dark border';
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 'bg-warning-subtle text-warning border border-warning-subtle';
      case 'PUBLISHED':
        return 'bg-success-subtle text-success border border-success-subtle';
      case 'REJECTED':
        return 'bg-danger-subtle text-danger border border-danger-subtle';
      default:
        return 'bg-light text-dark border';
    }
  }

  editEvent(eventId: number): void {
    this.router.navigate(['/events/edit', eventId]);
  }

  openDeleteDialog(eventId: number): void {
    this.eventIdToDelete = eventId;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.eventIdToDelete === null) return;

    this.deleteLoading = true;
    this.eventService.deleteEvent(this.eventIdToDelete).subscribe({
      next: () => {
        this.events = this.events.filter(e => e.idEvent !== this.eventIdToDelete);
        this.filterEvents();
        this.closeDeleteDialog();
        this.deleteLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Не удалось удалить мероприятие';
        this.deleteLoading = false;
        this.closeDeleteDialog();
      }
    });
  }

  closeDeleteDialog(): void {
    this.showDeleteDialog = false;
    this.eventIdToDelete = null;
  }

  get currentList(): EventCard[] {
    return this.displayType === 'active' ? this.activeEvents : this.archivedEvents;
  }

  openParticipants(eventId: number, eventName: string): void {
    this.selectedEventForParticipants = {id: eventId, name: eventName};
    this.showParticipantsModal = true;
  }

  closeParticipantsModal(): void {
    this.showParticipantsModal = false;
    this.selectedEventForParticipants = null;
  }
}
