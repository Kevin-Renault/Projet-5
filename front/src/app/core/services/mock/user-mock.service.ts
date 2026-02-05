import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { User } from '../../models/user.model';
import { MOCK_USERS } from '../../../shared/mock/mock-users.data';
import { UserDataSource } from '../user-datasource.interface';



@Injectable({ providedIn: 'root' })
export class UserMockService implements UserDataSource {
    getAll(): Observable<User[]> {
        return of(MOCK_USERS);
    }
    getById(id: number): Observable<User> {
        return of(MOCK_USERS.find(user => user.id === id)!);
    }
    create(user: User): Observable<User> {
        return of({ ...user, id: Date.now() });
    }
    update(id: number, user: Partial<User>): Observable<User> {
        return of({ ...MOCK_USERS.find(userMock => userMock.id === id), ...user } as User);
    }
    delete(id: number): Observable<void> {
        return of(void 0);
    }
}
