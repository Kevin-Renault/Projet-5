import { SlicePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

@Component({
  selector: 'app-profile',
  imports: [DynamicFormComponent, FormsModule, SlicePipe],
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

  topics = [

    {
      title: 'Topic 2',
      content: 'Content of Topic 2...',
      subscribed: true
    },
    {
      title: 'Topic 3',
      content: 'Content of Topic 3...',
      subscribed: true
    }
  ];

  onFormSubmit(values: any) {
    alert('Form submitted: ' + JSON.stringify(values, null, 2));
  }

  public toggleSubscription(topic: any) {
    topic.subscribed = !topic.subscribed;
  }
}
