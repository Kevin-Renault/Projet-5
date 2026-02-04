import { Observable } from 'rxjs';
import { Subscription } from '../models/subscription.model';

export interface SubscriptionDatasource {
    subscribe(userId: number, topicId: number): Observable<void>;
    unsubscribe(userId: number, topicId: number): Observable<void>;
    getUserSubscriptions(userId: number): Observable<Subscription[]>;
    getTopicSubscribers(topicId: number): Observable<Subscription[]>;
}
