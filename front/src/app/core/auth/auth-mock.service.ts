import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthMockService implements AuthDataSource {
    private readonly tokenKey = 'auth_token';
    private readonly mockUser: User = {
        id: 1,
        username: 'Mock User',
        email: 'mock@email.com',
        password: 'mockpassword',
        role: 'user'
    };

    login(email: string, password: string): Observable<AuthResponse> {
        // Simule un login réussi
        const token = 'mock-jwt-token';
        this.setToken(token);

        return of({ token, user: this.mockUser });
    }

    register(data: any): Observable<AuthResponse> {
        // Simule une inscription réussie
        const token = 'mock-jwt-token';
        this.setToken(token);
        return of({ token, user: { ...this.mockUser, ...data } });
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
    }

    setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    getCurrentUser(): Observable<any> {
        return of(this.mockUser);
    }
}
