import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AUTH_DATASOURCE } from 'src/app/core/auth/auth-datasource.interface';
import { FormElement, DynamicFormComponent, DynamicFormValues } from 'src/app/shared/form/dynamic-form.component';
import { ErrorComponent } from "src/app/shared/error/error.component";

@Component({
  selector: 'app-login',
  imports: [DynamicFormComponent, ErrorComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  private readonly authDataSource = inject(AUTH_DATASOURCE);
  private readonly router = inject(Router);

  connnectionFormElements: FormElement[] = [
    { type: 'text', name: 'login', label: 'E-mail ou Nom d\'utilisateur', required: true },
    {
      type: 'password',
      name: 'password',
      label: 'Mot de passe',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];
  errorMessage: string | null = null;

  onFormSubmit(values: DynamicFormValues) {
    const login = values['login'];
    const password = values['password'];

    if (typeof login !== 'string' || typeof password !== 'string') {
      this.errorMessage = 'Please fill in login and password.';
      return;
    }

    this.authDataSource.login(login, password).subscribe({
      next: () => {
        this.router.navigateByUrl('/articles');
      },
      error: () => {
        this.errorMessage = 'Authentication failed: invalid credentials';
      }
    });
  }
}
