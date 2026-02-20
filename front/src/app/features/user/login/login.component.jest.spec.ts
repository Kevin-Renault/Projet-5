import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AUTH_DATASOURCE } from '../../../core/auth/auth-datasource.interface';
import { LoginComponent } from './login.component';

describe('LoginComponent (integration jest)', () => {
    it('shows validation error when form values are not strings', () => {
        TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { login: jest.fn(() => of(void 0)) } },
                { provide: Router, useValue: { navigateByUrl: jest.fn() } },
            ],
        });

        const fixture = TestBed.createComponent(LoginComponent);
        const component = fixture.componentInstance;

        component.onFormSubmit({ login: true, password: 'x' } as any);
        expect(component.errorMessage).toContain('Please fill in');
    });

    it('navigates to /articles on successful login', () => {
        const login = jest.fn(() => of(void 0));
        const navigateByUrl = jest.fn();

        TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { login } },
                { provide: Router, useValue: { navigateByUrl } },
            ],
        });

        const fixture = TestBed.createComponent(LoginComponent);
        const component = fixture.componentInstance;

        component.onFormSubmit({ login: 'bob', password: 'Password#1' });
        expect(login).toHaveBeenCalledWith('bob', 'Password#1');
        expect(navigateByUrl).toHaveBeenCalledWith('/articles');
    });

    it('shows error message on login failure', () => {
        const login = jest.fn(() => throwError(() => new Error('nope')));
        const navigateByUrl = jest.fn();

        TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { login } },
                { provide: Router, useValue: { navigateByUrl } },
            ],
        });

        const fixture = TestBed.createComponent(LoginComponent);
        const component = fixture.componentInstance;

        component.onFormSubmit({ login: 'bob', password: 'bad' });
        expect(component.errorMessage).toContain('Authentication failed');
        expect(navigateByUrl).not.toHaveBeenCalled();
    });
});
