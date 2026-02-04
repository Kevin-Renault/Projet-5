import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { TopicSubscription } from '../../models/topic-subscription.model';
import { TopicSubscriptionDatasource } from '../topic-subscription-datasource.interface';

let MOCK_SUBSCRIPTIONS: TopicSubscription[] = [
    // alice (1) : Java (1), Base de données (6)
    { userId: 1, topicId: 1 },
    { userId: 1, topicId: 6 },
    // bob (2) : Angular (2), DevOps (5), Java (1)
    { userId: 2, topicId: 2 },
    { userId: 2, topicId: 5 },
    { userId: 2, topicId: 1 },
    // charlie (3) : Python (3), Web (4), Angular (2)
    { userId: 3, topicId: 3 },
    { userId: 3, topicId: 4 },
    { userId: 3, topicId: 2 },
    // diana (4) : Java (1), Web (4), DevOps (5)
    { userId: 4, topicId: 1 },
    { userId: 4, topicId: 4 },
    { userId: 4, topicId: 5 },
    // eve (5) : Python (3), Base de données (6), Angular (2)
    { userId: 5, topicId: 3 },
    { userId: 5, topicId: 6 },
    { userId: 5, topicId: 2 }
];

@Injectable({ providedIn: 'root' })
export class TopicSubscriptionMockService implements TopicSubscriptionDatasource {
    private subscriptions: TopicSubscription[] = [...MOCK_SUBSCRIPTIONS];

    subscribeOnTopic(userId: number, topicId: number): Observable<TopicSubscription[]> {
        const exists = this.subscriptions.some(s => s.userId === userId && s.topicId === topicId);
        if (!exists) {
            this.subscriptions.push({ userId, topicId });
        }
        return this.getUserTopicSubscriptions(userId);
    }

    unsubscribeFromTopic(userId: number, topicId: number): Observable<TopicSubscription[]> {
        this.subscriptions = this.subscriptions.filter(s => !(s.userId === userId && s.topicId === topicId));
        return this.getUserTopicSubscriptions(userId);
    }

    getUserTopicSubscriptions(userId: number): Observable<TopicSubscription[]> {
        return of(this.subscriptions.filter(s => s.userId === userId));
    }

    getTopicSubscribers(topicId: number): Observable<TopicSubscription[]> {
        return of(this.subscriptions.filter(s => s.topicId === topicId));
    }
}
