import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SUBSCRIPTION_DATASOURCE } from '../../core/services/topic-subscription-datasource.interface';
import { TOPIC_DATASOURCE } from '../../core/services/topic-datasource.interface';
import { TopicComponent } from './topic.component';
import { AUTH_DATASOURCE } from '../../core/auth/auth-datasource.interface';

describe('TopicComponent (integration jest)', () => {
    it('computes subscription state and toggles subscription', async () => {
        const getAll = jest.fn(() => of([{ id: 1, name: 'T1' }] as any));
        const getUserTopicSubscriptions = jest.fn(() => of([{ topicId: 1 }] as any));
        const subscribeOnTopic = jest.fn(() => of([] as any));
        const unsubscribeFromTopic = jest.fn(() => of([] as any));

        TestBed.configureTestingModule({
            imports: [TopicComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll } },
                {
                    provide: SUBSCRIPTION_DATASOURCE,
                    useValue: { getUserTopicSubscriptions, subscribeOnTopic, unsubscribeFromTopic },
                },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(TopicComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        expect(component.isSubscribed(1)).toBe(true);
        component.toggleTopicSubscription(2);
        expect(subscribeOnTopic).toHaveBeenCalledWith(2);

        component.toggleTopicSubscription(1);
        expect(unsubscribeFromTopic).toHaveBeenCalledWith(1);
    });

    it('sets error/message on failed toggle', async () => {
        TestBed.configureTestingModule({
            imports: [TopicComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll: () => of([]) } },
                {
                    provide: SUBSCRIPTION_DATASOURCE,
                    useValue: {
                        getUserTopicSubscriptions: () => of([]),
                        subscribeOnTopic: () => throwError(() => new Error('nope')),
                        unsubscribeFromTopic: jest.fn(),
                    },
                },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(TopicComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.toggleTopicSubscription(1);
        expect(component.error()).toBe(true);
        expect(component.message()).toContain('Erreur');
    });
});
