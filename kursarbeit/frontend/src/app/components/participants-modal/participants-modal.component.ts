import {Component, Input, Output, EventEmitter, OnInit, inject} from '@angular/core';
import {DatePipe} from '@angular/common';
import {ReactiveFormsModule, FormControl} from '@angular/forms';
import {EventService} from "../../services/event.service";
import {Participant, StatusOption} from "../../models/participant.model";

@Component({
  selector: 'app-participants-modal',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './participants-modal.component.html',
  styleUrls: ['./participants-modal.component.scss']
})
export class ParticipantsModalComponent implements OnInit {
  private eventService = inject(EventService);

  @Input() eventId!: number;
  @Input() eventName!: string;
  @Output() closeModal = new EventEmitter<void>();

  participants: Participant[] = [];
  isLoading = true;
  editingUserId: number | null = null;
  updatingUserId: number | null = null;

  editingStatusControl = new FormControl<string>('', {nonNullable: true});

  readonly statusOptions: StatusOption[] = [
    {id: 'REGISTERED', name: 'Зарегистрирован'},
    {id: 'ATTENDED', name: 'Посетил'},
    {id: 'CANCELLED', name: 'Отменено'},
    {id: 'WAITLISTED', name: 'Лист ожидания'},
    {id: 'REJECTED_BY_ORGANIZER', name: 'Отклонён организатором'}
  ];

  ngOnInit(): void {
    this.loadParticipants();
  }

  loadParticipants(): void {
    this.isLoading = true;
    this.eventService.getAllParticipants(this.eventId).subscribe({
      next: (data) => {
        this.participants = data;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  getStatusClass(status: string): string {
    return `status-badge status-${status}`;
  }

  getStatusName(status: string): string {
    return this.statusOptions.find(s => s.id === status)?.name || status;
  }

  startEdit(participant: Participant): void {
    if (this.updatingUserId) return;
    this.editingUserId = participant.userId;
    this.editingStatusControl.setValue(participant.participationStatus);
  }

  cancelEdit(): void {
    if (!this.updatingUserId) {
      this.editingUserId = null;
    }
  }

  saveStatus(participant: Participant): void {
    const newStatus = this.editingStatusControl.value;
    if (newStatus === participant.participationStatus) {
      this.editingUserId = null;
      return;
    }

    this.updatingUserId = participant.userId;
    this.editingUserId = null;

    this.eventService.changeParticipantStatus(this.eventId, participant.userId, newStatus, true)
      .subscribe({
        next: () => {
          participant.participationStatus = newStatus;
          this.updatingUserId = null;
        },
        error: () => {
          this.updatingUserId = null;
          this.loadParticipants();
        }
      });
  }

  close(): void {
    this.closeModal.emit();
  }
}
