import { Observable } from 'rxjs';
import { TopicSubscription } from '../models/topic-subscription.model';
import { InjectionToken } from '@angular/core';

export const SUBSCRIPTION_DATASOURCE = new InjectionToken<TopicSubscriptionDatasource>('TopicSubscriptionDataSource');
export interface TopicSubscriptionDatasource {
    subscribeOnTopic(topicId: number): Observable<TopicSubscription[]>;
    unsubscribeFromTopic(topicId: number): Observable<TopicSubscription[]>;
    getUserTopicSubscriptions(): Observable<TopicSubscription[]>;
    getTopicSubscribers(topicId: number): Observable<TopicSubscription[]>;
}
