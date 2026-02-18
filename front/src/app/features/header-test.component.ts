import { Component, signal } from '@angular/core';
import { HeaderButton, HeaderComponent } from '../shared/header/header.component';
import { DynamicFormComponent, FormElement } from '../shared/form/dynamic-form.component';
import { CommonComponent } from '../shared/common-component';

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
                [message]="connectionMessage()"
                
                (formSubmit)="onFormSubmit('connection', $event)">
            </app-dynamic-form>
            <hr>

            <app-dynamic-form
                [title]="'Inscription'"
                [formElements]="inscriptionFormElements"
                [submitLabel]="INSCRIPTION_LABEL"
                [message]="inscriptionMessage()"
                (formSubmit)="onFormSubmit('inscription', $event)">
            </app-dynamic-form>
            <hr>


            <app-dynamic-form
                [title]="'Créer un nouvel article'"
                [formElements]="articleFormElements"
                [submitLabel]="'Créer'"
                [message]="articleMessage()"
                (formSubmit)="onFormSubmit('article', $event)">
            </app-dynamic-form>
            <hr>

            <app-dynamic-form
                [title]="'Profil Utilisateur'"
                [formElements]="profileFormElements"
                [submitLabel]="'Sauvegarder'"
                [message]="profileMessage()"
                (formSubmit)="onFormSubmit('profile', $event)">
            </app-dynamic-form>

            <hr>

            <app-dynamic-form
                [title]="'Full Featured Form'"
                [formElements]="fullFormElements"
                [submitLabel]="'Tester la soumission'"
                [message]="fullMessage()"
                (formSubmit)="onFormSubmit('full', $event)">
            </app-dynamic-form>
    `
})
export class HeaderTestComponent extends CommonComponent {

    readonly connectionMessage = signal<string | null>(null);
    readonly inscriptionMessage = signal<string | null>(null);
    readonly articleMessage = signal<string | null>(null);
    readonly profileMessage = signal<string | null>(null);
    readonly fullMessage = signal<string | null>(null);

    INSCRIPTION_LABEL = 'S\'inscrire';

    buttons: HeaderButton[] = [
        {
            label: 'Se déconnecter',
            icon: '',
            color: 'red',
            alt: 'Icône Déconnexion',
            cssClass: 'header__btn logout',
            action: () => this.setMessage('Déconnexion !'),
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

    onFormSubmit(form: 'connection' | 'inscription' | 'article' | 'profile' | 'full', values: any) {
        this.clearAllFormMessages();
        const message = `Form submitted successfully! : ${JSON.stringify(values)}`;
        console.log(message);
        switch (form) {
            case 'connection':
                this.connectionMessage.set(message);
                break;
            case 'inscription':
                this.inscriptionMessage.set(message);
                break;
            case 'article':
                this.articleMessage.set(message);
                break;
            case 'profile':
                this.profileMessage.set(message);
                break;
            case 'full':
                this.fullMessage.set(message);
                break;
        }
    }

    private clearAllFormMessages() {
        this.connectionMessage.set(null);
        this.inscriptionMessage.set(null);
        this.articleMessage.set(null);
        this.profileMessage.set(null);
        this.fullMessage.set(null);
    }


    private setMessage(msg: string) {
        this.message.set(msg);
    }
}

