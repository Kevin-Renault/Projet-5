import { Component } from '@angular/core';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

@Component({
  selector: 'app-article-create',
  imports: [DynamicFormComponent],
  templateUrl: './article-create.component.html',
  styleUrl: './article-create.component.scss'
})
export class ArticleCreateComponent {

  articleFormElements: FormElement[] = [
    {
      type: 'select', name: 'thème sélectionné', placeholder: 'Selectionner un thème', required: true, options: [
        { value: 'Java', label: 'Java' },
        { value: 'Angular', label: 'Angular' },
        { value: 'DevOps', label: 'DevOps' }
      ]
    },
    { type: 'text', name: 'Titre de l\'article', placeholder: 'Titre de l\'article', required: true },
    { type: 'textarea', name: 'Contenu de l\'article', placeholder: 'Contenu de l\'article', required: true }
  ];

  onFormSubmit(values: any) {
    alert('Form submitted: ' + JSON.stringify(values, null, 2));
  }

}
