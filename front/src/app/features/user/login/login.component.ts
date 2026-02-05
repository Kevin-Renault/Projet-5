import { Component, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

@Component({
  selector: 'app-login',
  imports: [DynamicFormComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
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


  constructor(
    @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) { }

  onFormSubmit(values: any) {
    this.authDataSource.login(values.name, values.password).subscribe({
      next: () => {
        this.router.navigate(['/articles']);
        alert('Form submitted: ' + JSON.stringify(values, null, 2));
      },
      error: () => {
        this.snackBar.open('Authentication failed: invalid credentials', 'Close', { duration: 3000 });
      }
    });
  }
}
