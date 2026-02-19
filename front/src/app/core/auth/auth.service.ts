import { Injectable, inject, signal, Signal, ErrorHandler } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, firstValueFrom, map, Observable, switchMap, take, tap } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';
import { Router } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';

import { CsrfTokenService } from './csrf-token.service';
@Injectable({ providedIn: 'root' })
export class AuthService implements AuthDataSource {

    private readonly http = inject(HttpClient);
    private readonly errorHandler = inject(ErrorHandler);
    private readonly router = inject(Router);
    private readonly apiUrl = '/api/auth';
    private readonly csrfTokenService = inject(CsrfTokenService);

    // Signaux privés pour gérer l'état
    private readonly _currentUser = signal<User | null>(null);
    private readonly _currentUserId = signal<number | null>(null);

    readonly currentUser = this._currentUser.asReadonly();
    readonly currentUserId = this._currentUserId.asReadonly();


    readonly #isLoggedIn = signal(false);
    readonly isloggedIn = this.#isLoggedIn.asReadonly();

    async initSession(): Promise<void> {
        // Ensure the XSRF cookie is present so POST/PUT/DELETE requests are accepted
        // when CSRF protection is enabled on the backend.
        try {
            await firstValueFrom(this.initCsrf());
        } catch {
            // Non-blocking: session init can still proceed.
        }

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
        return this.initCsrf().pipe(
            switchMap(() => this.http.post<void>(`${this.apiUrl}/refresh`, {}, { observe: 'response' })),
            map(() => { })
        );
    }

    private initCsrf(): Observable<void> {
        return this.http.get<void>(`${this.apiUrl}/csrf`, { observe: 'response' }).pipe(
            tap((resp) => {
                const token = resp.headers.get('X-XSRF-TOKEN');
                this.csrfTokenService.set(token);
            }),
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
        this.csrfTokenService.clear();
    }

    login(email: string, password: string): Observable<void> {
        return this.initCsrf().pipe(
            switchMap(() => this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password })),
            tap((response) => {
                this.updateSession(response.user, true);
            }),
            map(() => void 0),
            catchError((error: HttpErrorResponse) => {
                this.errorHandler.handleError(error);
                this.clearSession();
                throw error;
            })
        );
    }

    register(data: User): Observable<AuthResponse> {
        return this.initCsrf().pipe(
            switchMap(() => this.http.post<AuthResponse>(`${this.apiUrl}/register`, data)),
            tap((response) => this.updateSession(response.user))
        );
    }

    logout(): void {
        this.initCsrf().pipe(
            switchMap(() => this.http.post<void>(`${this.apiUrl}/logout`, {})),
            tap(() => {
                this.clearSession();
                // Navigate only after the backend cleared HttpOnly cookies.
                this.router.navigate(['/']);
            }),
            catchError((error: HttpErrorResponse) => {
                // Even if the call fails, make the UI consistent.
                this.clearSession();
                this.router.navigate(['/']);
                this.errorHandler.handleError(error);
                throw error;
            }),
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
