import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderButton, HeaderComponent } from "./shared/header/header.component";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [RouterOutlet, HeaderComponent]
})
export class AppComponent {
  isAuthenticated = true;
  title = 'front';
  buttons: HeaderButton[] = [];

  public constructor() {
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
        action: () => alert('Déconnexion !'),
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
        link: '/',
        icon: '',
        alt: 'Icône Thèmes',
        cssClass: 'header__btn themes',
      },
      {
        label: '',
        link: '/profil',
        icon: '/assets/profil.svg',
        alt: 'Icône Profil',
        cssClass: 'header__btn profile',
      }
    ];
  }
}
