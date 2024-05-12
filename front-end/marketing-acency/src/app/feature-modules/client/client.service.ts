import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Client } from './model/client.model';

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  private baseUrl = 'https://localhost:8443';

  constructor(private http: HttpClient) { }

  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>('https://localhost:8443/client/all/');
  }

  getClientByUserId(userId: number): Observable<Client> {
    return this.http.get<Client>('https://localhost:8443/client/byUserId/' + userId);
  }

  updateClient(client: Client): Observable<Client> {
    return this.http.put<Client>('https://localhost:8443/client/update', client);
  }
}
