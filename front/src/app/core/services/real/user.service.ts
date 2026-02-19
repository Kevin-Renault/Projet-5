import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
    private readonly apiUrl = '/api/users';

    constructor(private readonly http: HttpClient) { }

    getAll(): Observable<User[]> {
        return this.http.get<User[]>(this.apiUrl);
    }

    getById(id: number): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/${id}`);
    }

    update(user: Partial<User>): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}`, user);
    }

}
