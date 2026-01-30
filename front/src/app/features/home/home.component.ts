import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: true,
    imports: [MatButtonModule, CommonModule]
})
export class HomeComponent implements OnInit {
    constructor() { }

    ngOnInit(): void { }

    start() {
        alert('Commencez par lire le README et à vous de jouer !');
    }
}
