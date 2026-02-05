import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error',
  imports: [],
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss'],
  standalone: true
})
export class ErrorComponent {
  @Input() displayArticlesButton: boolean = false;
  @Input() attemptedAction?: string;
  @Input() errorMessage?: string;

  goHome() {
    globalThis.location.href = '/';
  }
  goArticles() {
    globalThis.location.href = '/articles';
  }
}
