import { Component } from '@angular/core';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

@Component({
  selector: 'app-profile',
  imports: [DynamicFormComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {
  profileFormElements: FormElement[] = [
    { type: 'text', name: 'name', placeholder: 'Username', required: true },
    { type: 'email', name: 'email', placeholder: 'email@email.fr', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
    {
      type: 'password',
      name: 'password',
      placeholder: 'Mot de passe',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];
  onFormSubmit(values: any) {
    alert('Form submitted: ' + JSON.stringify(values, null, 2));
  }
}
