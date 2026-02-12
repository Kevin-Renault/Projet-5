import { Component, Input } from '@angular/core';
import { HeaderButton } from '../header/header.component';

@Component({
  selector: 'app-header-buttons',
  standalone: true,
  imports: [],
  styleUrls: ['./header.component.scss'],
  template: `
    @for (button of buttons; track $index) {
      <button [class]="button.cssClass" (click)="onButtonClick(button)"
       type="button" [style.color]="button.color">
        @if (button.icon) {
          <ng-container>
            <img [src]="button.icon" [alt]="button.alt" />
          </ng-container>
        }
        @if (button.label) {
          {{ button.label }}
        }
      </button>
    }
  `
})
export class HeaderButtonsComponent {
  @Input() buttons: HeaderButton[] = [];
  @Input() onButtonClick: (button: HeaderButton) => void = () => { };
}
