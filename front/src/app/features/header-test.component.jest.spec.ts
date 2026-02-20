import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AUTH_DATASOURCE } from '../core/auth/auth-datasource.interface';
import { HeaderTestComponent } from './header-test.component';

describe('HeaderTestComponent (integration jest)', () => {
    it('routes messages to the correct form signal and clears others', async () => {
        TestBed.configureTestingModule({
            imports: [HeaderTestComponent],
            providers: [
                { provide: Router, useValue: { navigate: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { logout: jest.fn(), getCurrentUser: () => ({ id: 1 }) } },
            ],
        });

        const fixture = TestBed.createComponent(HeaderTestComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit('connection', { a: 1 });
        expect(component.connectionMessage()).toContain('Form submitted successfully');
        expect(component.inscriptionMessage()).toBeNull();

        component.onFormSubmit('inscription', { b: 2 });
        expect(component.inscriptionMessage()).toContain('"b":2');
        expect(component.connectionMessage()).toBeNull();
    });

    it('button action can set the CommonComponent message', () => {
        TestBed.configureTestingModule({
            imports: [HeaderTestComponent],
            providers: [
                { provide: Router, useValue: { navigate: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { logout: jest.fn(), getCurrentUser: () => ({ id: 1 }) } },
            ],
        });

        const fixture = TestBed.createComponent(HeaderTestComponent);
        const component = fixture.componentInstance;

        component.buttons[0].action?.();
        expect(component.message()).toBe('Déconnexion !');
    });
});
