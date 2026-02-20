import { inject, Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AUTH_DATASOURCE } from './auth-datasource.interface';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

    private readonly authDataSource = inject(AUTH_DATASOURCE);
    constructor(
        private readonly router: Router) { }

    async canActivate(): Promise<boolean | UrlTree> {
        if (this.authDataSource.isAuthenticated$()()) {
            return true;
        }

        try {
            await this.authDataSource.initSession();
            return this.authDataSource.isAuthenticated$()() ? true : this.router.parseUrl('/');
        } catch {
            return this.router.parseUrl('/');
        }
    }
}