import { Injectable, inject } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CsrfTokenService } from '../auth/csrf-token.service';

@Injectable()
export class CredentialsInterceptor implements HttpInterceptor {
    private readonly csrfTokenService = inject(CsrfTokenService);

    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const csrfToken = this.csrfTokenService.get();
        const needsCsrf = this.needsCsrfHeader(req);

        const reqWithCreds = req.clone({
            withCredentials: true,
            setHeaders: needsCsrf && csrfToken && !req.headers.has('X-XSRF-TOKEN')
                ? { 'X-XSRF-TOKEN': csrfToken }
                : {},
        });

        return next.handle(reqWithCreds);
    }

    private needsCsrfHeader(req: HttpRequest<unknown>): boolean {
        // Only protect unsafe methods.
        if (['GET', 'HEAD', 'OPTIONS'].includes(req.method.toUpperCase())) {
            return false;
        }
        // Limit to API calls; avoids touching external URLs.
        return req.url.includes('/api/');
    }
}