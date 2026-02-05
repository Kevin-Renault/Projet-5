
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthDataSource, AuthResponse } from './auth-datasource.interface';

@Injectable({ providedIn: 'root' })
export class AuthService implements AuthDataSource {
    private readonly apiUrl = '/api/auth';
    private readonly tokenKey = 'auth_token';

    constructor(private readonly http: HttpClient) { }

    login(email: string, password: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password });
    }

    register(data: any): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data);
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

    // À adapter selon ton backend pour récupérer l'utilisateur courant
    getCurrentUser(): Observable<any> {
        // Option 1 : décoder le token côté front (si JWT contient les infos)
        // Option 2 : requête API dédiée
        return this.http.get<any>(`${this.apiUrl}/me`);
    }
}
