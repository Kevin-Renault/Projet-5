import { Inject, Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { map, Observable } from 'rxjs';
import { AUTH_DATASOURCE, AuthDataSource } from './auth-datasource.interface';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
    constructor(
        @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
        private readonly router: Router) { }

    canActivate(): Observable<boolean | UrlTree> {
        return this.authDataSource.isAuthenticated$().pipe(
            map(isAuth => {
                return isAuth ? true : this.router.parseUrl('/user/login');
            })
        );
    }
}