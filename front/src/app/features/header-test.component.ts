import { Component } from '@angular/core';
import { HeaderButton, HeaderComponent } from '../shared/header/header.component';
import { DynamicFormComponent, FormElement } from '../shared/form/dynamic-form.component';

@Component({
    selector: 'app-header-test',
    standalone: true,
    imports: [HeaderComponent, DynamicFormComponent],
    template: `
            <app-header ></app-header>
            <app-dynamic-form
                [title]="'Se connecter'"
                [formElements]="connnectionFormElements"
                [submitLabel]="'Se connecter'"
                (formSubmit)="onFormSubmit($event)">
            </app-dynamic-form>
            <hr>

            <app-dynamic-form
                [title]="'Inscription'"
                [formElements]="inscriptionFormElements"
                [submitLabel]="INSCRIPTION_LABEL"
                (formSubmit)="onFormSubmit($event)">
            </app-dynamic-form>
            <hr>


            <app-dynamic-form
                [title]="'Créer un nouvel article'"
                [formElements]="articleFormElements"
                [submitLabel]="'Créer'"
                (formSubmit)="onFormSubmit($event)">
            </app-dynamic-form>
            <hr>

            <app-dynamic-form
                [title]="'Profil Utilisateur'"
                [formElements]="profileFormElements"
                [submitLabel]="'Sauvegarder'"
                (formSubmit)="onFormSubmit($event)">
            </app-dynamic-form>

            <hr>

            <app-dynamic-form
                [title]="'Full Featured Form'"
                [formElements]="fullFormElements"
                [submitLabel]="'Tester la soumission'"
                (formSubmit)="onFormSubmit($event)">
            </app-dynamic-form>
    `
})
export class HeaderTestComponent {

    INSCRIPTION_LABEL = 'S\'inscrire';

    buttons: HeaderButton[] = [
        {
            label: 'Se déconnecter',
            icon: '',
            color: 'red',
            alt: 'Icône Déconnexion',
            cssClass: 'header__btn logout',
            action: () => alert('Déconnexion !'),
        },
        {
            label: 'Articles',
            link: '/articles',
            color: 'violet',
            icon: '',
            alt: 'Icône Articles',
            cssClass: 'header__btn articles',
        },
        {
            label: 'Thèmes',
            link: '/',
            icon: '',
            alt: 'Icône Thèmes',
            cssClass: 'header__btn themes',
        },
        {
            label: '',
            link: '/profil',
            icon: '/assets/profil.svg',
            alt: 'Icône Profil',
            cssClass: 'header__btn profile',
        }
    ];

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



    fullFormElements: FormElement[] = [
        { type: 'text', name: 'username', label: 'Nom d\'utilisateur', placeholder: 'Votre nom', required: true },
        { type: 'email', name: 'email', label: 'Adresse e-mail', placeholder: 'Votre email', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
        { type: 'password', name: 'password', label: 'Mot de passe', placeholder: 'Votre mot de passe', required: true, pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$' },
        { type: 'textarea', name: 'bio', label: 'Bio', placeholder: 'Votre bio', required: false },
        { type: 'checkbox', name: 'accept', label: 'J\'accepte les conditions', required: true },
        {
            type: 'select', name: 'favorite', label: 'Bière préférée', placeholder: 'Choisissez une bière', required: true, options: [
                { value: 'lager', label: 'Lager' },
                { value: 'ipa', label: 'IPA' },
                { value: 'stout', label: 'Stout' },
                { value: 'pilsner', label: 'Pilsner' }
            ]
        }
    ];



    onFormSubmit(values: any) {
        console.log('Form submitted:', values);
    }
}

