import { inject, signal } from "@angular/core";
import { AUTH_DATASOURCE } from "../core/auth/auth-datasource.interface";


export class CommonComponent {


    protected readonly authDataSource = inject(AUTH_DATASOURCE);

    action = '';
    isLoading = signal(false);
    error = signal(false);
    message = signal<string | null>(null);

    constructor() {
        this.message.set('Initialisation en cours...');
    }

    startSubmit() {
        this.isLoading.set(true);
        this.error.set(false);
    }

}
