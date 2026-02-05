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
    register(data: any): Observable<AuthResponse>;
    logout(): void;
    setToken(token: string): void;
    getToken(): string | null;
    isAuthenticated(): boolean;
    getCurrentUser(): Observable<any>;
}
