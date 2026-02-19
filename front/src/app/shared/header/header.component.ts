import { Component, HostListener, inject } from '@angular/core';
import { HeaderButtonsComponent } from "./header-buttons.component";
import { Router } from '@angular/router';
import { AUTH_DATASOURCE } from 'src/app/core/auth/auth-datasource.interface';

@Component({
    selector: 'app-header',
    standalone: true,
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    imports: [HeaderButtonsComponent]
})
export class HeaderComponent {

    authDataSource = inject(AUTH_DATASOURCE);

    menuOpen = false;
    constructor(
        private readonly router: Router
    ) {
    }

    @HostListener('window:resize')
    onResize() {
        if (window.innerWidth > 800 && this.menuOpen) {
            this.menuOpen = false;
        }
    }
    onButtonClick(button: HeaderButton) {
        if (button.action) {
            button.action();
        } else if (button.link) {
            this.callNavigation(button.link);
        }
    }

    callNavigation(link: string) {

        this.router.navigate([link]);
    }

    toggleMenu() {
        this.menuOpen = !this.menuOpen;
    }

    logout() {
        this.authDataSource.logout();
    }

    buttons: HeaderButton[] = [
        {
            label: 'Se déconnecter',
            icon: '',
            color: 'red',
            alt: 'Icône Déconnexion',
            cssClass: 'header__btn logout',
            action: () => this.logout(),
        },
        {
            label: 'Articles',
            link: '/articles',
            color: 'violet',
            icon: '',
            alt: 'Icône Articles',
            cssClass: 'header__btn articles',
        },
        {
            label: 'Thèmes',
            link: '/topics',
            icon: '',
            alt: 'Icône Thèmes',
            cssClass: 'header__btn themes',
        },
        {
            label: '',
            link: '/user/profile',
            icon: '/assets/profil.svg',
            alt: 'Icône Profil',
            cssClass: 'header__btn profile',
        }
    ];
}

export interface HeaderButton {
    label: string;
    link?: string;
    color?: string;
    icon: string;
    alt: string;
    cssClass?: string;
    action?: () => void;
}










