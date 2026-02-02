import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { User } from '../../models/user.model';
import { UserDataSource } from '../user-datasource.interface';

const MOCK_USERS: User[] = [
    { id: 1, username: 'alice', email: 'alice@email.com', role: 'user' },
    { id: 2, username: 'bob', email: 'bob@email.com', role: 'user' }
];

@Injectable({ providedIn: 'root' })
export class UserMockService implements UserDataSource {
    getAll(): Observable<User[]> {
        return of(MOCK_USERS);
    }
    getById(id: number): Observable<User> {
        return of(MOCK_USERS.find(u => u.id === id)!);
    }
    create(user: User): Observable<User> {
        return of({ ...user, id: Date.now() });
    }
    update(id: number, user: Partial<User>): Observable<User> {
        return of({ ...MOCK_USERS.find(u => u.id === id), ...user } as User);
    }
    delete(id: number): Observable<void> {
        return of(void 0);
    }
}
