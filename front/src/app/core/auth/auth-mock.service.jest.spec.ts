import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthMockService } from './auth-mock.service';

describe('AuthMockService (jest)', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it('logs in with username/email + password and stores token', async () => {
        const navigate = jest.fn();
        TestBed.configureTestingModule({
            providers: [{ provide: Router, useValue: { navigate } }],
        });

        const service = TestBed.runInInjectionContext(() => new AuthMockService());
        await firstValueFrom(service.login('alice', 'Mock-password1'));

        const isAuth = service.isAuthenticated$();
        expect(isAuth()).toBe(true);
        expect(service.getToken()).toContain('mock-jwt-token');
        expect(service.getCurrentUser().username).toBe('alice');
    });

    it('fails login and clears auth state', async () => {
        const navigate = jest.fn();
        TestBed.configureTestingModule({
            providers: [{ provide: Router, useValue: { navigate } }],
        });

        const service = TestBed.runInInjectionContext(() => new AuthMockService());
        await expect(firstValueFrom(service.login('alice', 'bad'))).rejects.toBeTruthy();

        const isAuth = service.isAuthenticated$();
        expect(isAuth()).toBe(false);
        expect(service.getCurrentUser().id).toBe(-1);
    });

    it('registers a new user and marks authenticated', async () => {
        const navigate = jest.fn();
        TestBed.configureTestingModule({
            providers: [{ provide: Router, useValue: { navigate } }],
        });

        const service = TestBed.runInInjectionContext(() => new AuthMockService());
        const res = await firstValueFrom(
            service.register({ id: 0, username: 'new', email: 'new@email.com', password: 'Mock-passwordX', role: 'user' } as any)
        );

        const isAuth = service.isAuthenticated$();
        expect(isAuth()).toBe(true);
        expect(res.user.username).toBe('new');
        expect(service.getToken()).toContain('mock-jwt-token');
    });

    it('initSession throws and logs out when no token', async () => {
        const navigate = jest.fn();
        TestBed.configureTestingModule({
            providers: [{ provide: Router, useValue: { navigate } }],
        });

        const service = TestBed.runInInjectionContext(() => new AuthMockService());
        await expect(service.initSession()).rejects.toBeTruthy();
        expect(navigate).toHaveBeenCalledWith(['/']);
    });
});
