import {Component, OnInit, inject} from '@angular/core';
import {DatePipe} from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {AuthService} from '../../../services/auth.service';
import {Category, EventCreateDto} from '../../../models/event.models';
import {lastValueFrom} from 'rxjs';

@Component({
  selector: 'app-create-event',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, DatePipe],
  templateUrl: './create-event.component.html',
  styleUrls: ['./create-event.component.scss']
})
export class CreateEventComponent implements OnInit {
  private fb = inject(FormBuilder);
  private eventService = inject(EventService);
  private authService = inject(AuthService);
  private router = inject(Router);

  form!: FormGroup;
  categories: Category[] = [];
  isLoading = false;
  submitError: string | null = null;
  submitSuccess = false;

  today = new Date().toISOString().split('T')[0];

  ngOnInit(): void {
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
      recording: [false],
      specialNotes: ['']
    });
    this.loadCategories();
  }

  private futureDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const selectedDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return selectedDate < today ? {pastDate: true} : null;
  }

  private loadCategories(): void {
    this.eventService.getCategories().subscribe(cats => this.categories = cats);
  }

  get isOnline(): boolean {
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

    this.isLoading = true;
    this.submitError = null;

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.submitError = 'Пользователь не авторизован';
      this.isLoading = false;
      return;
    }

    const v = this.form.getRawValue();

    try {
      let placeId: number | null = null;

      if (v.placeType === 'PHYSICAL') {
        const addressParts = [];
        if (v.city) addressParts.push(v.city);
        if (v.street) addressParts.push(v.street.toLowerCase().startsWith('ул.') ? v.street : `ул. ${v.street}`);
        if (v.house) addressParts.push(v.house.toLowerCase().startsWith('д.') ? v.house : `д. ${v.house}`);

        const placeResp = await lastValueFrom(this.eventService.createPhysicalPlace({
          placeName: v.placeName || 'Не указано',
          placeDescription: null,
          address: addressParts.join(', '),
          disabilityAccessible: v.disabilityAccessible
        }));
        placeId = placeResp.idPlace;
      } else {
        const placeResp = await lastValueFrom(this.eventService.createOnlinePlace({
          placeName: v.placeName || 'Онлайн-встреча',
          placeDescription: null,
          meetingUrl: v.meetingUrl,
          specialNotes: v.specialNotes,
          recording: v.recording
        }));
        placeId = placeResp.idPlace;
      }

      const eventPayload: EventCreateDto = {
        idOrganizer: currentUser.userId,
        eventFormat: v.format,
        eventStatus: 'PLANNED',
        idEventCategory: Number(v.categoryId),
        idPlace: placeId,
        eventName: v.eventName,
        eventDescription: v.eventDescription || null,
        //eventDate: `${v.eventDate}T00:00:00`,
        eventDate: `${v.eventDate}T${v.startTime}:00`,
        startTime: `${v.eventDate}T${v.startTime}:00`,
        endTime: `${v.eventDate}T${v.endTime}:00`,
        maxParticipants: v.maxParticipants ? Number(v.maxParticipants) : null,
        imageUrl: v.imageUrl || null,
        price: v.price,
        verified: false,
        verificationComment: null
      };

      await lastValueFrom(this.eventService.createEvent(eventPayload));
      this.submitSuccess = true;
      setTimeout(() => this.router.navigate(['/events/manage']), 1500);
    } catch (err: any) {
      this.submitError = err.error?.message || 'Ошибка при создании мероприятия';
    } finally {
      this.isLoading = false;
    }
  }
}
