import { computed, Injectable, Signal, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Observable, of, throwError } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';
import { MOCK_USERS } from '../../shared/mock/mock-users.data';



@Injectable({ providedIn: 'root' })
export class AuthMockService implements AuthDataSource {
    private readonly isAuthenticatedSignal = signal(false);
    private readonly currentUserSignal = signal<User | null>(null);
    private readonly currentUserIdSignal = computed(() => this.currentUserSignal()?.id ?? null);
    private readonly isAuthenticatedObservable = toObservable(this.isAuthenticatedSignal);
    private readonly currentUserObservable = toObservable(this.currentUserSignal);
    // Synchronise l'état d'authentification avec le token localStorage au démarrage
    constructor() {
        const token = this.getToken();
        this.setAuthState(!!token);
        if (token) {
            // Restaure l'utilisateur courant depuis le mock si un token est présent
            try {
                const parsed = JSON.parse(token);
                if (parsed?.userId) {
                    const user = MOCK_USERS.find(u => u.id === parsed.userId) || null;
                    if (user) {
                        this.currentUserSignal.set(user);
                    }
                }
            } catch {
                // fallback: ignore
            }
        }
    }
    private readonly tokenKey = 'auth_token';

    async initSession(): Promise<void> {
        const token = this.getToken();
        if (!token) {
            this.logout();
            throw new Error('No session');
        }

        this.setAuthState(true);
        try {
            const parsed = JSON.parse(token);
            const userId = parsed?.userId;
            const user = MOCK_USERS.find(u => u.id === userId) || null;
            if (!user) {
                this.logout();
                throw new Error('Invalid session');
            }
            this.currentUserSignal.set(user);
        } catch {
            this.logout();
            throw new Error('Invalid session');
        }
    }

    refresh(): Observable<void> {
        // En mock: pas de refresh serveur, on considère la session valide si un token existe.
        const token = this.getToken();
        if (!token) {
            return throwError(() => new Error('No session'));
        }
        return of(void 0);
    }

    login(login: string, password: string): Observable<void> {
        const user = MOCK_USERS.find(u =>
            (u.email === login || u.username === login) && u.password === password
        );

        if (user) {
            const token = JSON.stringify({ token: 'mock-jwt-token', userId: user.id });
            this.setToken(token);
            this.setAuthState(true);
            this.currentUserSignal.set(user);
            return of(void 0);
        } else {
            this.setAuthState(false);
            this.currentUserSignal.set(null);
            return throwError(() => new Error('Identifiants invalides'));
        }
    }

    register(data: User): Observable<AuthResponse> {
        const newId = Math.max(...MOCK_USERS.map(u => u.id), 0) + 1;
        const newUser: User = {
            id: newId,
            username: data.username,
            email: data.email,
            password: data.password || `Mock-password${newId}`,
            role: 'user'
        };
        const token = JSON.stringify({ token: 'mock-jwt-token', userId: newUser.id });
        this.setToken(token);
        this.setAuthState(true);
        this.currentUserSignal.set(newUser);
        return of({ token, user: newUser });
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        this.setAuthState(false);
        this.currentUserSignal.set(null);
    }

    clearSession(): void {
        this.logout();
    }

    setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
        this.setAuthState(true);
    }

    private setAuthState(state: boolean): void {
        this.isAuthenticatedSignal.set(state);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isAuthenticated$(): Signal<boolean> {
        // À chaque souscription, on vérifie le token localStorage pour garantir la synchro
        const token = this.getToken();
        this.setAuthState(!!token);
        return this.isAuthenticatedSignal.asReadonly();
    }

    getCurrentUser(): User {
        return this.currentUserSignal() || { id: -1, username: '', email: '', password: '' };
    }

    refreshCurrentUser(): Observable<User> {
        return this.currentUserObservable.pipe(
            filter((user): user is User => user !== null),
        );
    }

    getCurrentUserId(): Observable<number | null> {
        return toObservable(this.currentUserIdSignal);
    }
}
