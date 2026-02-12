import { Component, Inject, Input } from '@angular/core';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';

@Component({
  selector: 'app-error',
  imports: [],
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss'],
  standalone: true
})
export class ErrorComponent {
  @Input() attemptedAction?: string;
  @Input() errorMessage?: string;


  public constructor(@Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource) {
  }


  goHome() {
    globalThis.location.href = '/';
  }
  goArticles() {
    globalThis.location.href = '/articles';
  }
}
