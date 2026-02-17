import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AUTH_DATASOURCE } from 'src/app/core/auth/auth-datasource.interface';
import { User } from 'src/app/core/models/user.model';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';
import { ErrorComponent } from "src/app/shared/error/error.component";

@Component({
  selector: 'app-register',
  imports: [DynamicFormComponent, ErrorComponent],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  private readonly authDataSource = inject(AUTH_DATASOURCE);
  private readonly router = inject(Router);

  INSCRIPTION_LABEL = 'S\'inscrire';
  inscriptionFormElements: FormElement[] = [
    { type: 'text', name: 'username', label: 'Nom d\'utilisateur', required: true },
    { type: 'email', name: 'email', label: 'Adresse e-mail', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
    {
      type: 'password',
      name: 'password',
      label: 'Password',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];

  errorMessage: string | null = null;

  onFormSubmit(user: User) {
    this.authDataSource.register(user).subscribe({
      next: () => {
        this.router.navigate(['/articles']);
      },
      error: () => {
        this.errorMessage = 'Authentication failed: invalid credentials';
      }
    });
  }
}

