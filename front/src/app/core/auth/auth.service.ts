
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService implements AuthDataSource {
    private readonly authState = new BehaviorSubject<boolean>(false);
    private readonly apiUrl = '/api/auth';

    constructor(private readonly http: HttpClient) { }

    getCurrentUserId(): number | null {
        throw new Error('Method not implemented.');
    }

    login(email: string, password: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
            tap(() => this.authState.next(true))
        );
    }

    register(data: any): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data);
    }

    logout(): void {
        this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
            next: () => this.authState.next(false),
            error: () => this.authState.next(false)
        });
    }

    isAuthenticated$(): Observable<boolean> {
        return this.authState.asObservable();
    }

    // Récupère l'utilisateur courant via l'API (cookie httpOnly envoyé automatiquement)
    getCurrentUser(): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/me`).pipe(
            tap({
                next: () => this.authState.next(true),
                error: () => this.authState.next(false)
            })
        );
    }
}
