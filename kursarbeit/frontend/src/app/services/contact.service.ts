import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../environment';

export interface ContactMessageDto {
  name: string;
  email: string;
  subject: string;
  message: string;
}

@Injectable({providedIn: 'root'})
export class ContactService {
  private readonly api = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  sendMessage(dto: ContactMessageDto): Observable<void> {
    return this.http.post<void>(`${this.api}/contact/send`, dto);
  }
}
