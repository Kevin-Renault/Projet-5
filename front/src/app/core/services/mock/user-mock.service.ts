import { Injectable, inject } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { User } from '../../models/user.model';
import { MOCK_USERS } from '../../../shared/mock/mock-users.data';
import { UserDataSource } from '../user-datasource.interface';
import { AUTH_DATASOURCE } from '../../auth/auth-datasource.interface';



@Injectable({ providedIn: 'root' })
export class UserMockService implements UserDataSource {
    private readonly authDataSource = inject(AUTH_DATASOURCE);

    getAll(): Observable<User[]> {
        return of(MOCK_USERS);
    }
    getById(id: number): Observable<User> {
        return of(MOCK_USERS.find(user => user.id === id)!);
    }

    update(user: Partial<User>): Observable<User> {
        const currentUser = this.authDataSource.getCurrentUser();
        const targetId = user.id ?? currentUser?.id;

        if (targetId == null || targetId < 0) {
            return throwError(() => new Error('No authenticated user'));
        }

        const existingIndex = MOCK_USERS.findIndex((u) => u.id === targetId);
        if (existingIndex === -1) {
            return throwError(() => new Error(`User with id ${targetId} not found`));
        }

        const existing = MOCK_USERS[existingIndex];
        Object.assign(existing, user);
        existing.id = targetId;
        return of(existing);
    }
}
