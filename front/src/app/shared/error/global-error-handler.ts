// global-error-handler.ts
import { ErrorHandler, inject, Injectable, Injector } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
    private readonly injector = inject(Injector);

    handleError(error: any): void {
        const router = this.injector.get(Router);

        if (error instanceof HttpErrorResponse) {
            // Gestion des erreurs HTTP
            console.error('Erreur HTTP:', error);

            if (error.status === 401) {
                console.error('Non autorisé - Redirection vers la page de login');
                router.navigate(['/user/login']);
            } else if (error.status === 404) {
                console.error('Ressource non trouvée');
                router.navigate(['/not-found']);
            } else if (error.status === 500) {
                console.error('Erreur serveur interne');
                router.navigate(['/']);
            } else {
                console.error('Erreur serveur:', error);
            }
        } else {
            // Gestion des autres erreurs (ex: erreurs JavaScript)
            console.error('Erreur inattendue:', error);
        }
    }
}
