import { Injectable, inject, signal, Signal, ErrorHandler } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, firstValueFrom, map, Observable, of, take, tap } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';
import { toObservable } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class AuthService implements AuthDataSource {

    private readonly http = inject(HttpClient);
    private readonly errorHandler = inject(ErrorHandler);
    private readonly apiUrl = '/api/auth';

    // Signaux privés pour gérer l'état
    private readonly _currentUser = signal<User | null>(null);
    private readonly _currentUserId = signal<number | null>(null);

    readonly currentUser = this._currentUser.asReadonly();
    readonly currentUserId = this._currentUserId.asReadonly();


    readonly #isLoggedIn = signal(false);
    readonly isloggedIn = this.#isLoggedIn.asReadonly();

    async initSession(): Promise<void> {
        try {
            await firstValueFrom(this.refreshCurrentUser());
        } catch {
            try {
                await firstValueFrom(this.refresh());
                await firstValueFrom(this.refreshCurrentUser());
            } catch {
                this.clearSession();
                throw new Error('Session expirée');
            }
        }
    }

    refresh(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/refresh`, {}, { observe: 'response' }).pipe(
            map(() => { })
        );
    }

    getCurrentUserId(): Observable<number | null> {
        return toObservable(this.currentUserId);
    }

    getCurrentUser(): User {
        return this.currentUser() || { id: -1, username: '', email: '', password: '' };
    }


    // Méthode pour satisfaire l'interface AuthDataSource
    isAuthenticated$(): Signal<boolean> {
        return this.isloggedIn;
    }

    private updateSession(user: User, isLoggedIn: boolean = true): void {
        this._currentUser.set(user);
        this._currentUserId.set(user.id);
        this.#isLoggedIn.set(isLoggedIn);
    }

    public clearSession(): void {
        this._currentUser.set(null);
        this._currentUserId.set(null);
        this.#isLoggedIn.set(false);
    }

    login(email: string, password: string): Observable<boolean> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
            tap((response) => {
                this.updateSession(response.user, true);
            }),
            map(() => this.isloggedIn()),
            catchError((error: HttpErrorResponse) => {
                this.errorHandler.handleError(error);
                this.clearSession();
                // Retourne false en cas d'erreur
                return of(false);
            })
        );
    }

    register(data: unknown): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
            tap((response) => this.updateSession(response.user))
        );
    }

    logout(): void {
        this.http.post<void>(`${this.apiUrl}/logout`, {}).pipe(
            tap(() => this.clearSession()),
            take(1) // <-- Gère automatiquement le désabonnement
        ).subscribe();
    }

    refreshCurrentUser(): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/me`).pipe(
            tap({
                next: (user) => this.updateSession(user),
                error: () => this.clearSession()
            })
        );
    }
}
