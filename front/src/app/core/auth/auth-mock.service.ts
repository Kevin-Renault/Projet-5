
import { Injectable } from '@angular/core';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';
import { MOCK_USERS } from '../../shared/mock/mock-users.data';


@Injectable({ providedIn: 'root' })
export class AuthMockService implements AuthDataSource {
    private readonly authState = new BehaviorSubject<boolean>(!!this.getToken());
    private readonly tokenKey = 'auth_token';


    private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    login(emailOrUsername: string, password: string): Observable<AuthResponse> {
        // Recherche d'un utilisateur correspondant à l'email OU au username et au mot de passe
        const user = MOCK_USERS.find(u =>
            (u.email === emailOrUsername || u.username === emailOrUsername) && u.password === password
        );
        if (user) {
            const token = 'mock-jwt-token';
            this.setToken(token);
            this.authState.next(true);
            this.currentUserSubject.next(user);
            return of({ token, user });
        } else {
            this.authState.next(false);
            throw new Error('Invalid credentials');
        }
    }

    register(data: any): Observable<AuthResponse> {
        // Simule une inscription réussie
        const token = 'mock-jwt-token';
        const newId = Math.max(...MOCK_USERS.map(u => u.id)) + 1;
        const newUser: User = {
            id: newId,
            username: data.username,
            email: data.email,
            password: data.password || `Mock-password${newId}`,
            role: 'user'
        };
        MOCK_USERS.push(newUser);
        this.setToken(token);
        this.authState.next(true);
        this.currentUserSubject.next(newUser);
        return of({ token, user: newUser });
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
