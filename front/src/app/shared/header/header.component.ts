import { Component, HostListener, Input, OnInit } from '@angular/core';
import { HeaderButtonsComponent } from "./header-buttons.component";

@Component({
    selector: 'app-header',
    standalone: true,
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    imports: [HeaderButtonsComponent]
})
export class HeaderComponent {
    @Input() buttons!: HeaderButton[];
    menuOpen = false;

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
        if (link.startsWith('/')) {
            globalThis.location.href = link;
        } else {
            window.open(link, '_blank');
        }
    }

    toggleMenu() {
        this.menuOpen = !this.menuOpen;
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



