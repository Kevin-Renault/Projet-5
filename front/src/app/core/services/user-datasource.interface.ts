import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { InjectionToken } from '@angular/core';

export const USER_DATASOURCE = new InjectionToken<UserDataSource>('UserDataSource');
export interface UserDataSource {
    getAll(): Observable<User[]>;
    getById(id: number): Observable<User>;
    create(user: User): Observable<User>;
    update(user: Partial<User>): Observable<User>;
}
