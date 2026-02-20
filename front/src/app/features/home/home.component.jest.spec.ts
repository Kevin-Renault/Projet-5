import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AUTH_DATASOURCE } from '../../core/auth/auth-datasource.interface';
import { HomeComponent } from './home.component';

describe('HomeComponent (integration jest)', () => {
    it('auto-navigates to /articles when initSession succeeds', async () => {
        const initSession = jest.fn(async () => void 0);
        const navigate = jest.fn();

        TestBed.configureTestingModule({
            imports: [HomeComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { initSession } },
                { provide: Router, useValue: { navigate } },
            ],
        });

        const fixture = TestBed.createComponent(HomeComponent);
        fixture.detectChanges();

        await Promise.resolve();
        expect(navigate).toHaveBeenCalledWith(['/articles']);
    });

    it('sets errorMessage when initSession fails', async () => {
        const initSession = jest.fn(async () => {
            throw new Error('no');
        });
        const navigate = jest.fn();

        TestBed.configureTestingModule({
            imports: [HomeComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { initSession } },
                { provide: Router, useValue: { navigate } },
            ],
        });

        const fixture = TestBed.createComponent(HomeComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();

        await Promise.resolve();
        expect(component.errorMessage).toContain('Initialisation');
        expect(navigate).not.toHaveBeenCalledWith(['/articles']);
    });

    it('navigates to login/register on button click', () => {
        const initSession = jest.fn(async () => void 0);
        const navigate = jest.fn();

        TestBed.configureTestingModule({
            imports: [HomeComponent],
            providers: [
                { provide: AUTH_DATASOURCE, useValue: { initSession } },
                { provide: Router, useValue: { navigate } },
            ],
        });

        const fixture = TestBed.createComponent(HomeComponent);
        const component = fixture.componentInstance;
        component.goToLogin();
        component.goToRegister();

        expect(navigate).toHaveBeenCalledWith(['/user/login']);
        expect(navigate).toHaveBeenCalledWith(['/user/register']);
    });
});
