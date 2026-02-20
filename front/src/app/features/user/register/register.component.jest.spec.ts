import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AUTH_DATASOURCE } from '../../../core/auth/auth-datasource.interface';
import { RegisterComponent } from './register.component';

describe('RegisterComponent (integration jest)', () => {
    it('shows validation error when form values are not strings', () => {
        TestBed.configureTestingModule({
            imports: [RegisterComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { register: jest.fn(() => of({} as any)) } },
                { provide: Router, useValue: { navigate: jest.fn() } },
            ],
        });

        const fixture = TestBed.createComponent(RegisterComponent);
        const component = fixture.componentInstance;

        component.onFormSubmit({ username: 'u', email: null, password: 'x' } as any);
        expect(component.errorMessage).toContain('Please fill in');
    });

    it('registers user and navigates to /articles on success', () => {
        const register = jest.fn(() => of({ token: 't', user: { id: 1 } } as any));
        const navigate = jest.fn();

        TestBed.configureTestingModule({
            imports: [RegisterComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { register } },
                { provide: Router, useValue: { navigate } },
            ],
        });

        const fixture = TestBed.createComponent(RegisterComponent);
        const component = fixture.componentInstance;

        component.onFormSubmit({ username: 'bob', email: 'bob@example.com', password: 'Password#1' });
        expect(register).toHaveBeenCalled();
        expect(navigate).toHaveBeenCalledWith(['/articles']);
    });

    it('shows error message on register failure', () => {
        const register = jest.fn(() => throwError(() => new Error('nope')));
        const navigate = jest.fn();

        TestBed.configureTestingModule({
            imports: [RegisterComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { register } },
                { provide: Router, useValue: { navigate } },
            ],
        });

        const fixture = TestBed.createComponent(RegisterComponent);
        const component = fixture.componentInstance;

        component.onFormSubmit({ username: 'bob', email: 'bob@example.com', password: 'bad' });
        expect(component.errorMessage).toContain('Authentication failed');
        expect(navigate).not.toHaveBeenCalled();
    });
});
