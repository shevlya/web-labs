import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environment';

@Injectable({ providedIn: 'root' })
export class OrganizerRequestService {
  private readonly api = `${environment.apiUrl}/organizer-requests`;

  constructor(private http: HttpClient) {}

  createRequest(requestText: string): Observable<any> {
    return this.http.post(`${this.api}/me`, { requestText });
  }
}
