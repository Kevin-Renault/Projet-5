import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { ErrorHandler } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from './auth.service';
import { CsrfTokenService } from './csrf-token.service';

describe('AuthService (jest)', () => {
    function setup() {
        const errorHandler = { handleError: jest.fn() } as Pick<ErrorHandler, 'handleError'>;

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, RouterTestingModule],
            providers: [AuthService, CsrfTokenService, { provide: ErrorHandler, useValue: errorHandler }],
        });

        return {
            service: TestBed.inject(AuthService),
            csrf: TestBed.inject(CsrfTokenService),
            httpMock: TestBed.inject(HttpTestingController),
            router: TestBed.inject(Router),
            errorHandler,
        };
    }

    afterEach(() => {
        // Ensure no pending HTTP calls leaked between tests
        try {
            TestBed.inject(HttpTestingController).verify();
        } catch {
            // ignore when setup() not called
        }
    });

    it('login initializes CSRF then updates session', (done) => {
        const { service, csrf, httpMock } = setup();

        service.login('a@b.com', 'Password#1').subscribe({
            next: () => {
                const isAuth = service.isAuthenticated$();
                expect(isAuth()).toBe(true);
                expect(service.getCurrentUser().id).toBe(1);
                expect(csrf.getToken()).toBe('csrf-1');
                done();
            },
            error: done,
        });

        const csrfReq = httpMock.expectOne('/api/auth/csrf');
        expect(csrfReq.request.method).toBe('GET');
        csrfReq.flush(null, { headers: new HttpHeaders({ 'X-XSRF-TOKEN': 'csrf-1' }) });

        const loginReq = httpMock.expectOne('/api/auth/login');
        expect(loginReq.request.method).toBe('POST');
        loginReq.flush({
            token: 'jwt',
            user: { id: 1, username: 'u', email: 'e', password: 'p', role: 'user' },
        });
    });

    it('login clears session and forwards error to ErrorHandler on failure', (done) => {
        const { service, csrf, httpMock, errorHandler } = setup();
        csrf.setToken('pre');

        service.login('a@b.com', 'wrong').subscribe({
            next: () => done(new Error('expected error')),
            error: (err: HttpErrorResponse) => {
                const isAuth = service.isAuthenticated$();
                expect(isAuth()).toBe(false);
                expect(service.getCurrentUser().id).toBe(-1);
                expect(csrf.getToken()).toBeNull();
                expect(errorHandler.handleError).toHaveBeenCalled();
                expect(err.status).toBe(401);
                done();
            },
        });

        httpMock.expectOne('/api/auth/csrf').flush(null, { headers: new HttpHeaders({ 'X-XSRF-TOKEN': 'csrf-2' }) });
        httpMock.expectOne('/api/auth/login').flush({ message: 'nope' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('refresh calls /refresh after CSRF init', (done) => {
        const { service, httpMock } = setup();

        service.refresh().subscribe({
            next: () => done(),
            error: done,
        });

        httpMock.expectOne('/api/auth/csrf').flush(null);
        const refreshReq = httpMock.expectOne('/api/auth/refresh');
        expect(refreshReq.request.method).toBe('POST');
        refreshReq.flush(null);
    });

    it('logout clears session and navigates home on success', () => {
        const { service, httpMock, router } = setup();
        jest.spyOn(router, 'navigate').mockResolvedValue(true);

        service.logout();

        httpMock.expectOne('/api/auth/csrf').flush(null);
        const req = httpMock.expectOne('/api/auth/logout');
        expect(req.request.method).toBe('POST');
        req.flush(null);

        const isAuth = service.isAuthenticated$();
        expect(isAuth()).toBe(false);
        expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('initSession falls back to refresh when /me fails once', async () => {
        const { service, httpMock } = setup();

        const promise = service.initSession();

        // initCsrf
        httpMock.expectOne('/api/auth/csrf').flush(null);

        // Let the async function continue after the awaited CSRF step
        await Promise.resolve();

        // first refreshCurrentUser fails
        httpMock.expectOne('/api/auth/me').flush({ message: 'expired' }, { status: 401, statusText: 'Unauthorized' });

        await Promise.resolve();

        // refresh path
        httpMock.expectOne('/api/auth/csrf').flush(null);
        httpMock.expectOne('/api/auth/refresh').flush(null);

        await Promise.resolve();

        // second refreshCurrentUser succeeds
        httpMock.expectOne('/api/auth/me').flush({ id: 2, username: 'u2', email: 'e2', password: 'p2', role: 'user' });

        await expect(promise).resolves.toBeUndefined();
        const isAuth = service.isAuthenticated$();
        expect(isAuth()).toBe(true);
    });
});
