import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { TOPIC_DATASOURCE } from '../../../core/services/topic-datasource.interface';
import { USER_DATASOURCE } from '../../../core/services/user-datasource.interface';
import { SUBSCRIPTION_DATASOURCE } from '../../../core/services/topic-subscription-datasource.interface';
import { AUTH_DATASOURCE } from '../../../core/auth/auth-datasource.interface';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent (integration jest)', () => {
    it('validates input types before updating', async () => {
        TestBed.configureTestingModule({
            imports: [ProfileComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll: () => of([]) } },
                { provide: SUBSCRIPTION_DATASOURCE, useValue: { getUserTopicSubscriptions: () => of([]), unsubscribeFromTopic: jest.fn() } },
                { provide: USER_DATASOURCE, useValue: { update: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(ProfileComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit({ username: 'u', email: 'e', password: null } as any);
        expect(component.error()).toBe(true);
        expect(component.message()).toContain('Veuillez renseigner');
    });

    it('updates user and sets success message', async () => {
        const update = jest.fn(() => of({ id: 1 } as any));

        TestBed.configureTestingModule({
            imports: [ProfileComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll: () => of([]) } },
                { provide: SUBSCRIPTION_DATASOURCE, useValue: { getUserTopicSubscriptions: () => of([]), unsubscribeFromTopic: jest.fn() } },
                { provide: USER_DATASOURCE, useValue: { update } },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(ProfileComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit({ username: 'u', email: 'e', password: 'Password#1' });
        expect(update).toHaveBeenCalledWith({ username: 'u', email: 'e', password: 'Password#1' });
        expect(component.message()).toContain('réussie');
        expect(component.isLoading()).toBe(false);
    });

    it('sets error on update failure', async () => {
        const update = jest.fn(() => throwError(() => new Error('nope')));

        TestBed.configureTestingModule({
            imports: [ProfileComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll: () => of([]) } },
                { provide: SUBSCRIPTION_DATASOURCE, useValue: { getUserTopicSubscriptions: () => of([]), unsubscribeFromTopic: jest.fn() } },
                { provide: USER_DATASOURCE, useValue: { update } },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(ProfileComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit({ username: 'u', email: 'e', password: 'Password#1' });
        expect(component.error()).toBe(true);
        expect(component.message()).toContain('Échec');
        expect(component.isLoading()).toBe(false);
    });
});
