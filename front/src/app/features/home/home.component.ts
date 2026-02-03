import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: true,
    imports: [MatButtonModule, CommonModule]
})
export class HomeComponent {
    constructor(private readonly router: Router) { }

    goToLogin() {
        this.router.navigate(['/user/login']);
    }

    goToRegister() {
        this.router.navigate(['/user/register']);
    }
}
