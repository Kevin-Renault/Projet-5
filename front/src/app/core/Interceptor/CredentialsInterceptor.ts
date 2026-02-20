import { Injectable, inject } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CsrfTokenService } from '../auth/csrf-token.service';

@Injectable()
export class CredentialsInterceptor implements HttpInterceptor {
    private readonly csrfTokenService = inject(CsrfTokenService);

    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        let patched = req.clone({ withCredentials: true });

        // When the backend uses an HttpOnly CSRF cookie, the browser cannot read it to
        // populate the XSRF header. We instead store the token from /api/auth/csrf response
        // header (X-XSRF-TOKEN) and replay it on state-changing requests.
        if (this.isUnsafeMethod(patched.method) && !patched.headers.has('X-XSRF-TOKEN')) {
            const csrfToken = this.csrfTokenService.getToken();
            if (csrfToken) {
                patched = patched.clone({
                    headers: patched.headers.set('X-XSRF-TOKEN', csrfToken),
                });
            }
        }

        return next.handle(patched);
    }

    private isUnsafeMethod(method: string): boolean {
        return method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS';
    }
}