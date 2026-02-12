import { Component, Inject, Signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { HeaderButton, HeaderComponent } from "./shared/header/header.component";
import { AUTH_DATASOURCE, AuthDataSource } from './core/auth/auth-datasource.interface';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [RouterOutlet, HeaderComponent]
})

export class AppComponent {
  isAuthenticated$: Signal<boolean>;
  title = 'front';
  buttons: HeaderButton[] = [];

  public constructor(@Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
    private readonly router: Router) {
    this.isAuthenticated$ = this.authDataSource.isAuthenticated$();
    this.buildMenu();
  }

  buildMenu() {
    this.buttons = [
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

  logout() {
    this.authDataSource.logout();
    this.router.navigate(['/user/login']);
  }
}
