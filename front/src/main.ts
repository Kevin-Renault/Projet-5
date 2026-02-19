import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app-routing.module';
import { environment } from './environments/environment';
import { enableProdMode, ErrorHandler } from '@angular/core';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi, withXsrfConfiguration } from '@angular/common/http';
import { CredentialsInterceptor } from './app/core/Interceptor/CredentialsInterceptor';
import { RefreshOn401Interceptor } from './app/core/Interceptor/RefreshOn401Interceptor';
import { GlobalErrorHandler } from './app/shared/error/global-error-handler';
import { provideDataSources } from './app/core/providers/data-sources.providers';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    provideRouter(routes),
    provideHttpClient(
      withInterceptorsFromDi(),
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' })
    ), // Configuration HTTP centralisée
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CredentialsInterceptor,
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: RefreshOn401Interceptor,
      multi: true,
    },
    ...provideDataSources({ useMock: environment.useMock }),
  ]
});