import { Observable } from 'rxjs';
import { Subscription } from '../models/subscription.model';
import { InjectionToken } from '@angular/core';

export const SUBSCRIPTION_DATASOURCE = new InjectionToken<SubscriptionDatasource>('SubscriptionDataSource');
export interface SubscriptionDatasource {
    subscribe(userId: number, topicId: number): Observable<void>;
    unsubscribe(userId: number, topicId: number): Observable<void>;
    getUserSubscriptions(userId: number): Observable<Subscription[]>;
    getTopicSubscribers(topicId: number): Observable<Subscription[]>;
}
