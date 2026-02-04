import { Observable } from 'rxjs';
import { Subscription } from '../models/subscription.model';
import { InjectionToken } from '@angular/core';

export const SUBSCRIPTION_DATASOURCE = new InjectionToken<SubscriptionDatasource>('SubscriptionDataSource');
export interface SubscriptionDatasource {
    subscribeOnTopic(userId: number, topicId: number): Observable<Subscription[]>;
    unsubscribeFromTopic(userId: number, topicId: number): Observable<Subscription[]>;
    getUserSubscriptions(userId: number): Observable<Subscription[]>;
    getTopicSubscribers(topicId: number): Observable<Subscription[]>;
}
