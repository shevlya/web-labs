import {Component, OnInit, inject} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {AuthService} from '../../../services/auth.service';
import {Category, EventDetail, EventSubmitChangesDto} from '../../../models/event.models';
import {lastValueFrom} from 'rxjs';

@Component({
  selector: 'app-edit-event',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, DatePipe],
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.scss']
})
export class EditEventComponent implements OnInit {
  private fb = inject(FormBuilder);
  private eventService = inject(EventService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  form!: FormGroup;
  categories: Category[] = [];
  isLoading = true;
  isSubmitting = false;
  submitError: string | null = null;
  submitSuccess = false;
  eventId!: number;
  originalEvent!: EventDetail;
  today = new Date().toISOString().split('T')[0];

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.eventId = Number(idParam);

    if (!idParam || isNaN(this.eventId)) {
      this.router.navigate(['/events/manage']);
      return;
    }

    this.initForm();
    this.loadInitialData();
  }

  private initForm(): void {
    this.form = this.fb.group({
      eventName: ['', [Validators.required, Validators.maxLength(200)]],
      eventDescription: [''],
      eventDate: ['', [Validators.required, this.futureDateValidator]],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      categoryId: [null, Validators.required],
      format: ['OFFLINE', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      maxParticipants: [null],
      imageUrl: [''],
      placeType: ['PHYSICAL', Validators.required],
      placeName: [''],
      city: [''],
      street: [''],
      house: [''],
      disabilityAccessible: [false],
      meetingUrl: [''],
      specialNotes: [''],
      recording: [false]
    });
  }

  private futureDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const selectedDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return selectedDate < today ? {pastDate: true} : null;
  }

  private async loadInitialData(): Promise<void> {
    try {
      this.categories = await lastValueFrom(this.eventService.getCategories());
      const event = await lastValueFrom(this.eventService.getEventById(this.eventId));
      this.originalEvent = event;

      const placeType = event.placeType === 'ONLINE' ? 'ONLINE' : 'PHYSICAL';

      this.form.patchValue({
        eventName: event.eventName,
        eventDescription: event.eventDescription,
        eventDate: event.eventDate.split('T')[0],
        startTime: event.startTime.slice(11, 16),
        endTime: event.endTime.slice(11, 16),
        categoryId: event.idEventCategory,
        format: event.eventFormat,
        price: event.price,
        maxParticipants: event.maxParticipants,
        imageUrl: event.imageUrl,
        placeType: placeType
      });

      if (event.idPlace) {
        if (placeType === 'ONLINE') {
          const place = await lastValueFrom(this.eventService.getOnlinePlace(event.idPlace));
          this.form.patchValue({
            placeName: place.placeName,
            meetingUrl: place.meetingUrl,
            specialNotes: place.specialNotes,
            recording: place.recording
          });
        } else {
          const place = await lastValueFrom(this.eventService.getPhysicalPlace(event.idPlace));
          const addr = this.parseAddress(place.address || '');
          this.form.patchValue({
            placeName: place.placeName,
            city: addr.city,
            street: addr.street,
            house: addr.house,
            disabilityAccessible: place.disabilityAccessible
          });
        }
      }
    } catch (err) {
      this.router.navigate(['/events/manage']);
    } finally {
      this.isLoading = false;
    }
  }

  private parseAddress(address: string) {
    const parts = address.split(',').map(p => p.trim());
    return {
      city: parts[0] || '',
      street: (parts[1] || '').replace(/^ул\.\s*/i, ''),
      house: (parts[2] || '').replace(/^д\.\s*/i, '')
    };
  }

  get isOnlineSelected(): boolean {
    return this.form.get('placeType')?.value === 'ONLINE';
  }

  fieldError(fieldName: string): boolean {
    const control = this.form.get(fieldName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.submitError = null;
    const v = this.form.getRawValue();

    try {
      const changes: EventSubmitChangesDto = {};
      if (v.eventName !== this.originalEvent.eventName) changes.eventName = v.eventName;
      if (v.eventDescription !== this.originalEvent.eventDescription) changes.eventDescription = v.eventDescription;
      if (v.categoryId !== this.originalEvent.idEventCategory) changes.idEventCategory = Number(v.categoryId);
      if (v.format !== this.originalEvent.eventFormat) changes.eventFormat = v.format;
      if (v.price !== this.originalEvent.price) changes.price = v.price;
      if (v.maxParticipants !== this.originalEvent.maxParticipants) changes.maxParticipants = v.maxParticipants ? Number(v.maxParticipants) : null;
      if (v.imageUrl !== this.originalEvent.imageUrl) changes.imageUrl = v.imageUrl || null;

      const newStart = `${v.eventDate}T${v.startTime}:00`;
      const newEnd = `${v.eventDate}T${v.endTime}:00`;

      if (newStart !== this.originalEvent.startTime) {
        changes.eventDate = newStart;
        changes.startTime = newStart;
      }
      if (newEnd !== this.originalEvent.endTime) {
        changes.endTime = newEnd;
      }

      const hasPlaceChanges = await this.checkPlaceChanges(v);
      if (hasPlaceChanges) {
        let newPlaceId: number;
        if (v.placeType === 'ONLINE') {
          const resp = await lastValueFrom(this.eventService.createOnlinePlace({
            placeName: v.placeName || 'Онлайн-встреча',
            meetingUrl: v.meetingUrl,
            specialNotes: v.specialNotes,
            recording: v.recording
          }));
          newPlaceId = resp.idPlace;
        } else {
          const resp = await lastValueFrom(this.eventService.createPhysicalPlace({
            placeName: v.placeName || 'Не указано',
            address: this.buildAddress(v.city, v.street, v.house),
            disabilityAccessible: v.disabilityAccessible
          }));
          newPlaceId = resp.idPlace;
        }
        changes.idPlace = newPlaceId;
      }

      if (Object.keys(changes).length === 0) {
        this.submitError = 'Вы не внесли никаких изменений';
        this.isSubmitting = false;
        return;
      }

      await lastValueFrom(this.eventService.submitEventChanges(this.eventId, changes));
      this.submitSuccess = true;
      setTimeout(() => this.router.navigate(['/events/manage']), 1500);

    } catch (err: any) {
      this.submitError = err.error?.message || 'Ошибка при сохранении изменений';
    } finally {
      this.isSubmitting = false;
    }
  }

  private async checkPlaceChanges(v: any): Promise<boolean> {
    const oldType = this.originalEvent.placeType === 'ONLINE' ? 'ONLINE' : 'PHYSICAL';
    if (v.placeType !== oldType) return true;

    if (v.placeType === 'ONLINE') {
      const orig = await lastValueFrom(this.eventService.getOnlinePlace(this.originalEvent.idPlace!));
      return v.placeName !== orig.placeName || v.meetingUrl !== orig.meetingUrl || v.recording !== orig.recording;
    } else {
      const orig = await lastValueFrom(this.eventService.getPhysicalPlace(this.originalEvent.idPlace!));
      return v.placeName !== orig.placeName || this.buildAddress(v.city, v.street, v.house) !== orig.address || v.disabilityAccessible !== orig.disabilityAccessible;
    }
  }

  private buildAddress(city: string, street: string, house: string): string {
    const parts = [];
    if (city) parts.push(city);
    if (street) parts.push(street.toLowerCase().startsWith('ул.') ? street : `ул. ${street}`);
    if (house) parts.push(house.toLowerCase().startsWith('д.') ? house : `д. ${house}`);
    return parts.join(', ');
  }
}
