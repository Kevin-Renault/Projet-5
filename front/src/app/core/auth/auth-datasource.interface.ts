import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

export interface AuthResponse {
    token: string;
    user: User;
}
export const AUTH_DATASOURCE = new InjectionToken<AuthDataSource>('AuthDataSource');
export interface AuthDataSource {
    login(email: string, password: string): Observable<AuthResponse>;
    register(data: User): Observable<AuthResponse>;
    logout(): void;
    isAuthenticated$(): Observable<boolean>;
    getCurrentUser(): Observable<User>;
    getCurrentUserId(): number | null;
}
