import { Injectable } from '@angular/core';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthMockService implements AuthDataSource {
    private readonly authState = new BehaviorSubject<boolean>(!!this.getToken());
    private readonly tokenKey = 'auth_token';
    private readonly mockUser: User = {
        id: 1,
        username: 'Mock User',
        email: 'mock@email.com',
        password: 'Mock-password1',
        role: 'user'
    };

    private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    login(email: string, password: string): Observable<AuthResponse> {
        // Vérification mockée des identifiants
        if (email === this.mockUser.email && password === this.mockUser.password) {
            const token = 'mock-jwt-token';
            this.setToken(token);
            this.authState.next(true);
            this.currentUserSubject.next(this.mockUser);
            return of({ token, user: this.mockUser });
        } else {
            this.authState.next(false);
            throw new Error('Invalid credentials');
        }
    }

    register(data: any): Observable<AuthResponse> {
        // Simule une inscription réussie
        const token = 'mock-jwt-token';
        this.setToken(token);
        this.currentUserSubject.next({ ...this.mockUser, ...data });
        return of({ token, user: { ...this.mockUser, ...data } });
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        this.authState.next(false);
    }

    setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
        this.authState.next(true);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isAuthenticated$(): Observable<boolean> {
        return this.authState.asObservable();
    }

    getCurrentUser(): Observable<User | null> {
        return this.currentUser$;
    }
}
