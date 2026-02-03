import { Component } from '@angular/core';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

@Component({
  selector: 'app-register',
  imports: [DynamicFormComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  INSCRIPTION_LABEL = 'S\'inscrire';
  inscriptionFormElements: FormElement[] = [
    { type: 'text', name: 'name', label: 'Nom d\'utilisateur', required: true },
    { type: 'email', name: 'email', label: 'Adresse e-mail', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
    {
      type: 'password',
      name: 'password',
      label: 'Password',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];

  onFormSubmit(values: any) {
    alert('Form submitted: ' + JSON.stringify(values, null, 2));
  }
}
