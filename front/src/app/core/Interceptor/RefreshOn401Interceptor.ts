import { Injectable, inject } from '@angular/core';
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';
import { AUTH_DATASOURCE } from '../auth/auth-datasource.interface';

@Injectable()
export class RefreshOn401Interceptor implements HttpInterceptor {
    private readonly auth = inject(AUTH_DATASOURCE);

    private refresh$?: Observable<void>;

    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        // Never attempt refresh on auth endpoints that would create loops.
        if (this.isAuthEndpoint(req.url)) {
            return next.handle(req);
        }

        return next.handle(req).pipe(
            catchError((error: unknown) => {
                if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
                    return throwError(() => error);
                }

                // Access token likely expired: refresh once, then retry original request.
                return this.getOrStartRefresh().pipe(
                    switchMap(() => next.handle(req)),
                    catchError((refreshError: unknown) => {
                        // Refresh failed -> session is no longer valid.
                        this.auth.clearSession();
                        return throwError(() => refreshError);
                    })
                );
            })
        );
    }

    private getOrStartRefresh(): Observable<void> {
        if (!this.refresh$) {
            this.refresh$ = this.auth.refresh().pipe(
                shareReplay(1),
                finalize(() => {
                    this.refresh$ = undefined;
                })
            );
        }
        return this.refresh$;
    }

    private isAuthEndpoint(url: string): boolean {
        // Covers absolute or relative URLs.
        return (
            url.includes('/api/auth/login') ||
            url.includes('/api/auth/register') ||
            url.includes('/api/auth/logout') ||
            url.includes('/api/auth/refresh')
        );
    }
}
