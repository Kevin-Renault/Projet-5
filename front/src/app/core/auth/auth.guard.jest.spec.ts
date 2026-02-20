import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { AUTH_DATASOURCE, AuthDataSource } from './auth-datasource.interface';
import { AuthGuard } from './auth.guard';

describe('AuthGuard (jest)', () => {
    it('allows navigation when already authenticated', async () => {
        const isAuth = signal(true);
        const authDataSource: Partial<AuthDataSource> = {
            isAuthenticated$: () => isAuth.asReadonly(),
            initSession: jest.fn(async () => void 0),
        };

        TestBed.configureTestingModule({
            imports: [RouterTestingModule],
            providers: [{ provide: AUTH_DATASOURCE, useValue: authDataSource }],
        });

        const guard = TestBed.runInInjectionContext(() => new AuthGuard(TestBed.inject(Router)));
        const result = await guard.canActivate();

        expect(result).toBe(true);
        expect(authDataSource.initSession).not.toHaveBeenCalled();
    });

    it('initializes session then allows when authentication becomes true', async () => {
        const isAuth = signal(false);
        const authDataSource: Partial<AuthDataSource> = {
            isAuthenticated$: () => isAuth.asReadonly(),
            initSession: jest.fn(async () => {
                isAuth.set(true);
            }),
        };

        TestBed.configureTestingModule({
            imports: [RouterTestingModule],
            providers: [{ provide: AUTH_DATASOURCE, useValue: authDataSource }],
        });

        const guard = TestBed.runInInjectionContext(() => new AuthGuard(TestBed.inject(Router)));
        const result = await guard.canActivate();
        expect(result).toBe(true);
    });

    it('redirects to / when initSession resolves but remains unauthenticated', async () => {
        const isAuth = signal(false);
        const authDataSource: Partial<AuthDataSource> = {
            isAuthenticated$: () => isAuth.asReadonly(),
            initSession: jest.fn(async () => void 0),
        };

        TestBed.configureTestingModule({
            imports: [RouterTestingModule],
            providers: [{ provide: AUTH_DATASOURCE, useValue: authDataSource }],
        });

        const router = TestBed.inject(Router);
        const guard = TestBed.runInInjectionContext(() => new AuthGuard(router));
        const result = await guard.canActivate();

        expect(result).toEqual(router.parseUrl('/'));
    });

    it('redirects to / when initSession throws', async () => {
        const isAuth = signal(false);
        const authDataSource: Partial<AuthDataSource> = {
            isAuthenticated$: () => isAuth.asReadonly(),
            initSession: jest.fn(async () => {
                throw new Error('boom');
            }),
        };

        TestBed.configureTestingModule({
            imports: [RouterTestingModule],
            providers: [{ provide: AUTH_DATASOURCE, useValue: authDataSource }],
        });

        const router = TestBed.inject(Router);
        const guard = TestBed.runInInjectionContext(() => new AuthGuard(router));
        const result = await guard.canActivate();
        expect(result).toEqual(router.parseUrl('/'));
    });
});
