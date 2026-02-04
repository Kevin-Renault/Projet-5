import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Subscription } from '../../models/subscription.model';
import { SubscriptionDatasource } from '../subscription-datasource.interface';

@Injectable({ providedIn: 'root' })
export class SubscriptionMockService implements SubscriptionDatasource {
    private subscriptions: Subscription[] = [];

    subscribe(userId: number, topicId: number): Observable<void> {
        const exists = this.subscriptions.some(s => s.userId === userId && s.topicId === topicId);
        if (!exists) {
            this.subscriptions.push({ userId, topicId });
        }
        return of();
    }

    unsubscribe(userId: number, topicId: number): Observable<void> {
        this.subscriptions = this.subscriptions.filter(s => !(s.userId === userId && s.topicId === topicId));
        return of();
    }

    getUserSubscriptions(userId: number): Observable<Subscription[]> {
        return of(this.subscriptions.filter(s => s.userId === userId));
    }

    getTopicSubscribers(topicId: number): Observable<Subscription[]> {
        return of(this.subscriptions.filter(s => s.topicId === topicId));
    }
}
