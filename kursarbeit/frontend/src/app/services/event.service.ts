import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {environment} from '../environment';
import {
  EventCard,
  EventDetail,
  EventCreateDto,
  EventSubmitChangesDto,
  Category,
  Place,
  PhysicalPlaceCreateDto,
  OnlinePlaceCreateDto,
  EventParticipant
} from '../models/event.models';
import {AuthService} from './auth.service';

@Injectable({providedIn: 'root'})
export class EventService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  getVerifiedEvents(): Observable<EventCard[]> {
    return this.http.get<EventCard[]>(`${this.api}/events/verified`);
  }

  getActiveEvents(): Observable<EventCard[]> {
    return this.http.get<EventCard[]>(`${this.api}/events/active`);
  }

  searchEvents(keyword: string): Observable<EventCard[]> {
    return this.http.get<EventCard[]>(`${this.api}/events/search?keyword=${keyword}`);
  }

  getEventById(id: number): Observable<EventDetail> {
    return this.http.get<EventDetail>(`${this.api}/events/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.api}/categories`);
  }

  getPlaces(): Observable<Place[]> {
    return this.http.get<Place[]>(`${this.api}/places`);
  }

  getPhysicalPlace(id: number): Observable<any> {
    return this.http.get(`${this.api}/physical-places/${id}`);
  }

  getOnlinePlace(id: number): Observable<any> {
    return this.http.get(`${this.api}/online-places/${id}`);
  }

  createPhysicalPlace(data: PhysicalPlaceCreateDto): Observable<any> {
    return this.http.post(`${this.api}/physical-places`, data);
  }

  createOnlinePlace(data: OnlinePlaceCreateDto): Observable<any> {
    return this.http.post(`${this.api}/online-places`, data);
  }

  createEvent(dto: EventCreateDto): Observable<EventDetail> {
    return this.http.post<EventDetail>(`${this.api}/events`, dto);
  }

  getEventsByOrganizer(organizerId: number): Observable<EventCard[]> {
    return this.http.get<EventCard[]>(`${this.api}/events/organizer/${organizerId}`);
  }

  deleteEvent(eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/events/${eventId}`);
  }

  submitEventChanges(eventId: number, changes: EventSubmitChangesDto): Observable<EventDetail> {
    return this.http.put<EventDetail>(`${this.api}/events/${eventId}/submit`, changes);
  }

  getUserParticipations(userId: number): Observable<EventParticipant[]> {
    return this.http.get<EventParticipant[]>(`${this.api}/event-participants/users/${userId}`);
  }

  getAllParticipants(eventId: number): Observable<EventParticipant[]> {
    return this.http.get<EventParticipant[]>(`${this.api}/event-participants/events/${eventId}`);
  }

  getRegisteredParticipants(eventId: number): Observable<EventParticipant[]> {
    return this.http.get<EventParticipant[]>(`${this.api}/event-participants/events/${eventId}/registered`);
  }

  getRegisteredParticipantsCount(eventId: number): Observable<number> {
    return this.http.get<number>(`${this.api}/event-participants/events/${eventId}/count`);
  }

  isUserRegistered(eventId: number): Observable<boolean> {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return of(false);
    const userId = currentUser.userId;
    return this.http.get<EventParticipant>(`${this.api}/event-participants/events/${eventId}/participants/${userId}`).pipe(
      map(p => p.participationStatus === 'REGISTERED' || p.participationStatus === 'WAITLISTED'),
      catchError(err => err.status === 404 ? of(false) : throwError(() => err))
    );
  }

  registerForEvent(eventId: number): Observable<EventParticipant> {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) throw new Error('Not authenticated');
    return this.http.post<EventParticipant>(`${this.api}/event-participants/register`, {
      userId: currentUser.userId,
      eventId
    });
  }

  cancelRegistration(eventId: number): Observable<EventParticipant> {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) throw new Error('Not authenticated');
    return this.http.post<EventParticipant>(
      `${this.api}/event-participants/${currentUser.userId}/events/${eventId}/cancel?sendEmail=true`, {}
    );
  }

  changeParticipantStatus(eventId: number, userId: number, newStatus: string, sendEmail = true): Observable<EventParticipant> {
    return this.http.patch<EventParticipant>(
      `${this.api}/event-participants/events/${eventId}/participants/${userId}/status?newStatus=${newStatus}&sendEmail=${sendEmail}`, {}
    );
  }

  // Рекомендованные мероприятия (авторизованный — по интересам, иначе — последние активные)
  getRecommendedEvents(limit = 6): Observable<EventCard[]> {
    return this.http.get<EventCard[]>(`${this.api}/recommendations/events?limit=${limit}`);
  }
}
