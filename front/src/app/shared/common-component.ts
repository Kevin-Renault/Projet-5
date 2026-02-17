import { computed, inject, signal } from "@angular/core";
import { AUTH_DATASOURCE } from "../core/auth/auth-datasource.interface";


export class CommonComponent {


    protected readonly authDataSource = inject(AUTH_DATASOURCE);

    action = '';
    isLoading = signal(false);

    protected readonly _isPageLoading = signal(false);

    protected computeIsPageLoading(): boolean {
        return true;
    }

    readonly isPageLoading = computed(() => this.computeIsPageLoading());

    error = signal(false);
    message = signal<string | null>(null);

    startSubmit() {
        this.isLoading.set(true);
        this.error.set(false);
    }

}
