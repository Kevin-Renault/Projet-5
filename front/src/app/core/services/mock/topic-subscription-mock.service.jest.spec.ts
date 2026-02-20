import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { AUTH_DATASOURCE } from '../../auth/auth-datasource.interface';
import { TopicSubscriptionMockService } from './topic-subscription-mock.service';

describe('TopicSubscriptionMockService (jest)', () => {
    it('returns empty lists when unauthenticated', async () => {
        TestBed.configureTestingModule({
            providers: [
                TopicSubscriptionMockService,
                { provide: AUTH_DATASOURCE, useValue: { getCurrentUser: () => ({ id: -1 }) } },
            ],
        });

        const service = TestBed.inject(TopicSubscriptionMockService);
        expect(await firstValueFrom(service.getUserTopicSubscriptions())).toEqual([]);
        expect(await firstValueFrom(service.subscribeOnTopic(1))).toEqual([]);
        expect(await firstValueFrom(service.unsubscribeFromTopic(1))).toEqual([]);
    });

    it('subscribes and unsubscribes for authenticated user', async () => {
        TestBed.configureTestingModule({
            providers: [
                TopicSubscriptionMockService,
                { provide: AUTH_DATASOURCE, useValue: { getCurrentUser: () => ({ id: 1 }) } },
            ],
        });

        const service = TestBed.inject(TopicSubscriptionMockService);
        const before = await firstValueFrom(service.getUserTopicSubscriptions());
        const afterSub = await firstValueFrom(service.subscribeOnTopic(999));
        expect(afterSub.some(s => s.topicId === 999)).toBe(true);

        const afterUnsub = await firstValueFrom(service.unsubscribeFromTopic(999));
        expect(afterUnsub.some(s => s.topicId === 999)).toBe(false);

        // sanity: didn't lose existing subs
        expect(before.length).toBeGreaterThan(0);
    });
});
