import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AUTH_DATASOURCE } from 'src/app/core/auth/auth-datasource.interface';
import { HeaderComponent } from './header.component';

describe('HeaderComponent (integration jest)', () => {
    it('toggles menu and closes on resize when width > 800', () => {
        const navigate = jest.fn();
        const logout = jest.fn();

        TestBed.configureTestingModule({
            imports: [HeaderComponent],
            providers: [
                { provide: Router, useValue: { navigate } },
                { provide: AUTH_DATASOURCE, useValue: { logout } },
            ],
        });

        const fixture = TestBed.createComponent(HeaderComponent);
        const component = fixture.componentInstance;

        expect(component.menuOpen).toBe(false);
        component.toggleMenu();
        expect(component.menuOpen).toBe(true);

        Object.defineProperty(window, 'innerWidth', { value: 900, configurable: true });
        component.onResize();
        expect(component.menuOpen).toBe(false);
    });

    it('navigates when a button has link and executes action when provided', () => {
        const navigate = jest.fn();
        const logout = jest.fn();

        TestBed.configureTestingModule({
            imports: [HeaderComponent],
            providers: [
                { provide: Router, useValue: { navigate } },
                { provide: AUTH_DATASOURCE, useValue: { logout } },
            ],
        });

        const fixture = TestBed.createComponent(HeaderComponent);
        const component = fixture.componentInstance;

        component.onButtonClick({ label: 'X', link: '/articles', icon: '', alt: '' });
        expect(navigate).toHaveBeenCalledWith(['/articles']);

        const action = jest.fn();
        component.onButtonClick({ label: 'Y', action, icon: '', alt: '' });
        expect(action).toHaveBeenCalled();

        component.logout();
        expect(logout).toHaveBeenCalled();
    });
});
