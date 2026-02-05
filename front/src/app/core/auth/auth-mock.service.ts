import { Injectable, OnDestroy } from '@angular/core';
import { Observable, of, BehaviorSubject, throwError, Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';
import { MOCK_USERS } from '../../shared/mock/mock-users.data';

@Injectable({ providedIn: 'root' })
export class AuthMockService implements AuthDataSource, OnDestroy {
    private readonly authState = new BehaviorSubject<boolean>(false);
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
                        this.currentUserSubject.next(user);
                    }
                }
            } catch {
                // fallback: ignore
            }
        }
    }
    private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
    private readonly destroy$ = new Subject<void>();
    private readonly tokenKey = 'auth_token';
    public currentUser$ = this.currentUserSubject.asObservable();

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
        this.authState.complete();
        this.currentUserSubject.complete();
    }

    login(login: string, password: string): Observable<AuthResponse> {
        const user = MOCK_USERS.find(u =>
            (u.email === login || u.username === login) && u.password === password
        );

        if (user) {
            const token = JSON.stringify({ token: 'mock-jwt-token', userId: user.id });
            this.setToken(token);
            this.setAuthState(true);
            this.currentUserSubject.next(user);
            return of({ token, user });
        } else {
            this.setAuthState(false);
            return throwError(() => new Error('Identifiants invalides'));
        }
    }

    register(data: Omit<User, 'id' | 'role'> & { password?: string }): Observable<AuthResponse> {
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
        this.currentUserSubject.next(newUser);
        return of({ token, user: newUser });
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        this.setAuthState(false);
        this.currentUserSubject.next(null);
    }

    setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
        this.setAuthState(true);
    }
    private setAuthState(state: boolean): void {
        this.authState.next(state);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isAuthenticated$(): Observable<boolean> {
        // À chaque souscription, on vérifie le token localStorage pour garantir la synchro
        const token = this.getToken();
        if (!!token !== this.authState.value) {
            this.setAuthState(!!token);
        }
        return this.authState.asObservable();
    }

    getCurrentUser(): Observable<User> {
        return this.currentUser$.pipe(
            filter((user): user is User => user !== null),
            takeUntil(this.destroy$)
        );
    }

    getCurrentUserId(): number | null {
        const user = this.currentUserSubject.value;
        console.log('Current user in getCurrentUserId:', user);
        return user ? user.id : null;
    }
}
