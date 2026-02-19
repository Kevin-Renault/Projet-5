import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CsrfTokenService {
    private token?: string;

    set(token: string | null | undefined): void {
        if (token && token.trim().length > 0) {
            this.token = token;
        }
    }

    get(): string | undefined {
        return this.token;
    }

    clear(): void {
        this.token = undefined;
    }
}
