import { computed, Injectable, inject, signal, Signal, ErrorHandler } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, firstValueFrom, map, Observable, of, take, tap } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService implements AuthDataSource {

    private readonly http = inject(HttpClient);
    private readonly errorHandler = inject(ErrorHandler);
    private readonly apiUrl = '/api/auth';

    // Signaux privés pour gérer l'état
    private readonly _currentUser = signal<User | null>(null);

    readonly currentUser = this._currentUser.asReadonly();
    readonly currentUserId = computed(() => this._currentUser()?.id ?? null);


    readonly #isLoggedIn = signal(false);
    readonly isloggedIn = this.#isLoggedIn.asReadonly();

    async initSession(): Promise<void> {
        try {
            await firstValueFrom(this.getCurrentUser());
        } catch {
            try {
                await firstValueFrom(this.refresh());
                await firstValueFrom(this.getCurrentUser());
            } catch {
                this.clearSession();
                throw new Error('Session expirée');
            }
        }
    }

    refresh(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/refresh`, { observe: 'response' }).pipe(
            tap((response) => {
                console.log('Réponse complète du backend pour refresh:', response);
            }),
            map(() => { }) // Convertit la réponse en void
        );
    }
    getCurrentUserId(): number | null {
        return this.currentUserId();
    }

    // Méthode pour satisfaire l'interface AuthDataSource
    isAuthenticated$(): Signal<boolean> {
        return this.isloggedIn;
    }

    private updateSession(user: User, isLoggedIn: boolean = true): void {
        this._currentUser.set(user);
        this.#isLoggedIn.set(isLoggedIn);
    }

    public clearSession(): void {
        this._currentUser.set(null);
        this.#isLoggedIn.set(false);
    }

    login(email: string, password: string): Observable<boolean> {
        console.info("Auth service login : " + email + " / " + password);

        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
            tap((response) => {
                this.updateSession(response.user, true);
                console.log('Réponse du backend pour login:', response);
                console.info("isloggedIn: " + this.isloggedIn());
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
            tap((response) => {
                console.log('Réponse du backend pour logout:', response);
            }),
            tap(() => this.clearSession()),
            take(1) // <-- Gère automatiquement le désabonnement
        ).subscribe();
    }

    getCurrentUser(): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/me`).pipe(
            tap({
                next: (user) => this.updateSession(user),
                error: () => this.clearSession()
            })
        );
    }


}
