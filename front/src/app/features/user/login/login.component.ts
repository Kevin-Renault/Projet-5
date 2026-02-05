import { Component, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';
import { ErrorComponent } from "src/app/shared/error/error.component";
import { AsyncPipe } from '@angular/common';
import { User } from 'src/app/core/models/user.model';

@Component({
  selector: 'app-login',
  imports: [DynamicFormComponent, ErrorComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  connnectionFormElements: FormElement[] = [
    { type: 'text', name: 'name', label: 'E-mail ou Nom d\'utilisateur', required: true },
    {
      type: 'password',
      name: 'password',
      label: 'Password',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];
  errorMessage: string | null = null;

  constructor(
    @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
    private readonly router: Router
  ) { }

  onFormSubmit(values: User) {
    this.authDataSource.login(values.username, values.password).subscribe({
      next: () => {
        this.router.navigate(['/articles']);
      },
      error: () => {
        this.errorMessage = 'Authentication failed: invalid credentials';
      }
    });
  }
}
