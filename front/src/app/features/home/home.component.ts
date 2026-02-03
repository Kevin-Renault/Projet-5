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

    start() {
        alert('Commencez par lire le README et à vous de jouer !');
    }

    goToRegister() {
        this.router.navigate(['/user/register']);
    }
}
