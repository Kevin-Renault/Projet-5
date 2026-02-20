import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { AUTH_DATASOURCE } from '../../auth/auth-datasource.interface';
import { UserMockService } from './user-mock.service';

describe('UserMockService (jest)', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it('throws when updating without authenticated user', async () => {
        TestBed.configureTestingModule({
            providers: [
                UserMockService,
                { provide: AUTH_DATASOURCE, useValue: { getCurrentUser: () => ({ id: -1 }) } },
            ],
        });

        const service = TestBed.inject(UserMockService);
        await expect(firstValueFrom(service.update({ username: 'x' }))).rejects.toBeTruthy();
    });

    it('updates the current user when authenticated', async () => {
        TestBed.configureTestingModule({
            providers: [
                UserMockService,
                { provide: AUTH_DATASOURCE, useValue: { getCurrentUser: () => ({ id: 1 }) } },
            ],
        });

        const service = TestBed.inject(UserMockService);
        const updated = await firstValueFrom(service.update({ username: 'alice2' }));
        expect(updated.id).toBe(1);
        expect(updated.username).toBe('alice2');
    });
});
