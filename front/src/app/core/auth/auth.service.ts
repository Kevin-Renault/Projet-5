
import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService implements AuthDataSource {
    private readonly isAuthenticatedSignal = signal(false);
    private readonly currentUserSignal = signal<User | null>(null);
    private readonly currentUserIdSignal = computed(() => this.currentUserSignal()?.id ?? null);
    private readonly isAuthenticatedObservable = toObservable(this.isAuthenticatedSignal);
    private readonly apiUrl = '/api/auth';

    constructor(private readonly http: HttpClient) { }

    getCurrentUserId(): number | null {
        return this.currentUserIdSignal();
    }

    login(email: string, password: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
            tap((response) => {
                this.currentUserSignal.set(response.user);
                this.isAuthenticatedSignal.set(true);
            })
        );
    }

    register(data: any): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
            tap((response) => {
                this.currentUserSignal.set(response.user);
                this.isAuthenticatedSignal.set(true);
            })
        );
    }

    logout(): void {
        this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
            next: () => {
                this.currentUserSignal.set(null);
                this.isAuthenticatedSignal.set(false);
            },
            error: () => {
                this.currentUserSignal.set(null);
                this.isAuthenticatedSignal.set(false);
            }
        });
    }

    isAuthenticated$(): Observable<boolean> {
        return this.isAuthenticatedObservable;
    }

    // Récupère l'utilisateur courant via l'API (cookie httpOnly envoyé automatiquement)
    getCurrentUser(): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/me`).pipe(
            tap({
                next: (user) => {
                    this.currentUserSignal.set(user);
                    this.isAuthenticatedSignal.set(true);
                },
                error: () => {
                    this.currentUserSignal.set(null);
                    this.isAuthenticatedSignal.set(false);
                }
            })
        );
    }
}
