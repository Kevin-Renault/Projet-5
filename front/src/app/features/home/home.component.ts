import { Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/auth/auth.service';
import { ErrorComponent } from "src/app/shared/error/error.component";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: true,
    imports: [MatButtonModule, CommonModule, ErrorComponent]
})
export class HomeComponent implements OnInit {

    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);

    errorMessage: string | null = null;
    ngOnInit(): void {
        this.authService.initSession().then(() => {
            this.router.navigate(['/articles']);
        })
            .catch((error) => {
                this.errorMessage = 'Initialisation automatique de la session échouée';
            });
    }

    goToLogin() {
        this.router.navigate(['/user/login']);
    }

    goToRegister() {
        this.router.navigate(['/user/register']);
    }
}
