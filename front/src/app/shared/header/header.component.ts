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

    get buttons(): HeaderButton[] {
        const currentUrl = this.router.url;
        return [
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
                color: currentUrl.startsWith('/articles') ? 'violet' : 'black',
                icon: '',
                alt: 'Icône Articles',
                cssClass: 'header__btn articles' + (currentUrl.startsWith('/articles') ? ' header__btn--active' : ''),
            },
            {
                label: 'Thèmes',
                link: '/topics',
                color: currentUrl.startsWith('/topics') ? 'violet' : 'black',
                icon: '',
                alt: 'Icône Thèmes',
                cssClass: 'header__btn themes' + (currentUrl.startsWith('/topics') ? ' header__btn--active' : ''),
            },
            {
                label: '',
                link: '/user/profile',
                icon: '/assets/profil.svg',
                alt: 'Icône Profil',
                cssClass: 'header__btn profile' + (currentUrl.startsWith('/user/profile') ? ' header__btn--active' : ''),
            }
        ];
    }
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










