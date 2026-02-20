import { HttpErrorResponse, HttpHandler, HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Observable, of, Subject, throwError } from 'rxjs';
import { AUTH_DATASOURCE } from '../auth/auth-datasource.interface';
import { RefreshOn401Interceptor } from './RefreshOn401Interceptor';

describe('RefreshOn401Interceptor (jest)', () => {
    it('does not attempt refresh for auth endpoints', (done) => {
        const refresh = jest.fn(() => of(void 0));
        const clearSession = jest.fn();

        TestBed.configureTestingModule({
            providers: [
                RefreshOn401Interceptor,
                { provide: AUTH_DATASOURCE, useValue: { refresh, clearSession } },
            ],
        });

        const interceptor = TestBed.inject(RefreshOn401Interceptor);

        const handler: HttpHandler = {
            handle: jest.fn(() => of({} as any)),
        };

        interceptor.intercept(new HttpRequest('POST', '/api/auth/login', {}), handler).subscribe({
            next: () => {
                expect(refresh).not.toHaveBeenCalled();
                expect(clearSession).not.toHaveBeenCalled();
                expect(handler.handle).toHaveBeenCalledTimes(1);
                done();
            },
            error: done,
        });
    });

    it('refreshes once and retries the original request on 401', (done) => {
        const refresh = jest.fn(() => of(void 0));
        const clearSession = jest.fn();

        TestBed.configureTestingModule({
            providers: [
                RefreshOn401Interceptor,
                { provide: AUTH_DATASOURCE, useValue: { refresh, clearSession } },
            ],
        });

        const interceptor = TestBed.inject(RefreshOn401Interceptor);

        let call = 0;
        const handler: HttpHandler = {
            handle: jest.fn((): Observable<any> => {
                call += 1;
                if (call === 1) {
                    return throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }));
                }
                return of({ ok: true } as any);
            }),
        };

        interceptor.intercept(new HttpRequest('GET', '/api/articles'), handler).subscribe({
            next: () => {
                expect(refresh).toHaveBeenCalledTimes(1);
                expect(handler.handle).toHaveBeenCalledTimes(2);
                expect(clearSession).not.toHaveBeenCalled();
                done();
            },
            error: done,
        });
    });

    it('clears session when refresh fails', (done) => {
        const refresh = jest.fn(() => throwError(() => new Error('refresh failed')));
        const clearSession = jest.fn();

        TestBed.configureTestingModule({
            providers: [
                RefreshOn401Interceptor,
                { provide: AUTH_DATASOURCE, useValue: { refresh, clearSession } },
            ],
        });

        const interceptor = TestBed.inject(RefreshOn401Interceptor);

        const handler: HttpHandler = {
            handle: jest.fn(() => throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }))),
        };

        interceptor.intercept(new HttpRequest('GET', '/api/users'), handler).subscribe({
            next: () => done(new Error('expected error')),
            error: () => {
                expect(refresh).toHaveBeenCalledTimes(1);
                expect(clearSession).toHaveBeenCalledTimes(1);
                done();
            },
        });
    });

    it('shares an in-flight refresh across concurrent 401s', (done) => {
        const refresh$ = new Subject<void>();
        const refresh = jest.fn(() => refresh$.asObservable());
        const clearSession = jest.fn();

        TestBed.configureTestingModule({
            providers: [
                RefreshOn401Interceptor,
                { provide: AUTH_DATASOURCE, useValue: { refresh, clearSession } },
            ],
        });

        const interceptor = TestBed.inject(RefreshOn401Interceptor);

        const attempts = new Map<string, number>();
        const handler: HttpHandler = {
            handle: jest.fn((req: HttpRequest<unknown>) => {
                const key = req.url;
                const nextAttempt = (attempts.get(key) ?? 0) + 1;
                attempts.set(key, nextAttempt);

                if (nextAttempt === 1) {
                    return throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }));
                }
                return of({ ok: true } as any);
            }),
        };

        let completed = 0;
        const maybeDone = () => {
            completed += 1;
            if (completed === 2) {
                expect(refresh).toHaveBeenCalledTimes(1);
                expect(clearSession).not.toHaveBeenCalled();
                // 2 initial + 2 retries
                expect(handler.handle).toHaveBeenCalledTimes(4);
                done();
            }
        };

        interceptor.intercept(new HttpRequest('GET', '/api/a'), handler).subscribe({ next: maybeDone, error: done });
        interceptor.intercept(new HttpRequest('GET', '/api/b'), handler).subscribe({ next: maybeDone, error: done });

        // release both
        refresh$.next();
        refresh$.complete();
    });
});
