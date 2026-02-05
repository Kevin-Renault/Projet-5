import { Observable } from 'rxjs';
import { TopicSubscription } from '../models/topic-subscription.model';
import { InjectionToken } from '@angular/core';

export const SUBSCRIPTION_DATASOURCE = new InjectionToken<TopicSubscriptionDatasource>('TopicSubscriptionDataSource');
export interface TopicSubscriptionDatasource {
    subscribeOnTopic(userId: number, topicId: number): Observable<TopicSubscription[]>;
    unsubscribeFromTopic(userId: number, topicId: number): Observable<TopicSubscription[]>;
    getUserTopicSubscriptions(userId: number): Observable<TopicSubscription[]>;
    getTopicSubscribers(topicId: number): Observable<TopicSubscription[]>;
}
