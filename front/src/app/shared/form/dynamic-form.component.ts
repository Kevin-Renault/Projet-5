import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, FormBuilder } from '@angular/forms';

export interface FormElement {
  type: 'text' | 'textarea' | 'email' | 'password' | 'select' | 'checkbox';
  name: string;
  label?: string;
  placeholder?: string;
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: string;
  value?: string;
  options?: { value: string; label: string }[];
}

@Component({
  selector: 'app-dynamic-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="dynamic-form">
      @if (title) {
        <h2>{{ title }}</h2>
      }
      @for (formElement of formElements; track formElement.name) {
        <fieldset class="form-group">
          <label [for]="formElement.name">
            {{ formElement.label }}
            @if (formElement.required && formElement.label) {
              <span aria-hidden="true">*</span>
            }
          </label>

          @switch (formElement.type) {
            @case ('textarea') {
              <textarea
                [id]="formElement.name"
                [formControlName]="formElement.name"
                [placeholder]="formElement.placeholder || ''"
                [ngClass]="getErrorClass(formElement.name)"
                [attr.aria-describedby]="formElement.name + '-error'"
              ></textarea>
            }
            @case ('select') {
              <select
                [id]="formElement.name"
                [formControlName]="formElement.name"
                [attr.required]="formElement.required ? true : null"
                [ngClass]="getErrorClass(formElement.name)"
                [attr.aria-describedby]="formElement.name + '-error'"
              >
                <option value="" disabled selected hidden *ngIf="formElement.placeholder">{{ formElement.placeholder }}</option>
                @for (opt of formElement.options ?? []; track $index) {
                  <option [value]="opt.value">{{ opt.label }}</option>
                }
              </select>
            }
            @case ('checkbox') {
              <input
                type="checkbox"
                [id]="formElement.name"
                [formControlName]="formElement.name"
                [ngClass]="getErrorClass(formElement.name)"
                [attr.aria-describedby]="formElement.name + '-error'"
              />
            }
            @default {
              <input
                [type]="formElement.type"
                [id]="formElement.name"
                [formControlName]="formElement.name"
                [placeholder]="formElement.placeholder || ''"
                [ngClass]="getErrorClass(formElement.name)"
                 [attr.aria-describedby]="formElement.name + '-error'"
              />
            }
          }

          @if (showError(formElement.name)) {
            <p [id]="formElement.name + '-error'" class="form-error" role="alert">
              {{ getErrorMessage(formElement.name) }}
            </p>
          }
        </fieldset>
      }
      <button type="submit" [disabled]="form.invalid">
        {{ submitLabel || 'Submit' }}

      </button>
      <output for="form-submit">
        @if (isLoading) {
        <p >Action en cours...</p>
        }
         @if (message) {          
        <p  class="success">{{ message }}</p>
        }
         @if (error) {          
        <p class="error">{{ error }}</p>
        }
      </output> 

    </form>
  `,
  styleUrls: ['./dynamic-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
// Composant de formulaire dynamique, génère un formulaire à partir d'une configuration passée en entrée
export class DynamicFormComponent implements OnChanges {
  @Input() formElements: FormElement[] = [];
  @Input() title?: string;
  @Input() submitLabel?: string = 'Submit';

  @Input() isLoading?: boolean = false;
  @Input() message?: string;
  @Input() error?: string;


  @Output() formSubmit = new EventEmitter<any>();

  // Groupe de contrôles du formulaire réactif
  form: FormGroup;

  // Initialisation du formulaire vide
  constructor(private readonly fb: FormBuilder) {
    this.form = this.fb.group({});
  }

  // Reconstruit le formulaire à chaque changement d'inputs (ex: nouveaux champs)
  ngOnChanges(): void {
    this.buildForm();
  }

  // Construit dynamiquement le groupe de contrôles et les validators à partir de la config
  private buildForm(): void {
    const group: { [key: string]: FormControl } = {};
    this.formElements.forEach(el => {
      const validators = [];
      if (el.required) validators.push(Validators.required);
      if (el.minLength) validators.push(Validators.minLength(el.minLength));
      if (el.maxLength) validators.push(Validators.maxLength(el.maxLength));
      if (el.pattern) validators.push(Validators.pattern(el.pattern));

      group[el.name] = new FormControl(el.value || '', {
        validators,
        nonNullable: true
      });
    });
    this.form = this.fb.group(group);
  }

  // Émet la valeur du formulaire si valide lors de la soumission
  onSubmit(): void {
    if (this.form.valid) {
      this.formSubmit.emit(this.form.getRawValue());
    }
  }

  // Affiche l'erreur si le champ est invalide et a été touché ou modifié
  showError(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control?.invalid && (control?.dirty || control?.touched);
  }

  // Retourne la classe CSS à appliquer selon la validité du champ (pour border color)
  getErrorClass(controlName: string): string {
    const control = this.form.get(controlName);
    if (!control) return '';
    if (control.invalid && (control.dirty || control.touched)) return 'input-error';
    if (control.valid && (control.dirty || control.touched)) return 'input-valid';
    return '';
  }

  // Génère le message d'erreur approprié selon la validation échouée
  getErrorMessage(controlName: string): string {
    const control = this.form.get(controlName);
    const formElement = this.formElements.find(e => e.name === controlName);
    if (!control?.errors || !formElement) return '';

    if (control.hasError('required'))
      return `${formElement.name} est obligatoire.`;
    if (control.hasError('minlength'))
      return `${formElement.name} est trop court (min ${control.errors['minlength'].requiredLength}).`;
    if (control.hasError('maxlength'))
      return `${formElement.name} est trop long (max ${control.errors['maxlength'].requiredLength}).`;
    if (control.hasError('pattern'))
      return `Le format de ${formElement.name} est invalide.`;
    return '';
  }
}
