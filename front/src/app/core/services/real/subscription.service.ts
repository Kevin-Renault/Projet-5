import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { TopicSubscription } from '../../models/topic-subscription.model';
import { TopicSubscriptionDatasource } from '../topic-subscription-datasource.interface';

@Injectable({ providedIn: 'root' })
export class TopicSubscriptionService implements TopicSubscriptionDatasource {
    public readonly API_PATH = '/api/subscriptions';

    constructor(private readonly http: HttpClient) { }

    subscribeOnTopic(topicId: number): Observable<TopicSubscription[]> {
        // Remplacer l'URL par celle de l'API réelle
        return this.http.post<TopicSubscription[]>(this.API_PATH, { topicId });
    }

    unsubscribeFromTopic(topicId: number): Observable<TopicSubscription[]> {
        // Remplacer l'URL par celle de l'API réelle
        return this.http.delete<TopicSubscription[]>(`${this.API_PATH}?topicId=${topicId}`);
    }

    getUserTopicSubscriptions(): Observable<TopicSubscription[]> {
        return this.http.get<TopicSubscription[]>(`${this.API_PATH}`);
    }

    getTopicSubscribers(topicId: number): Observable<TopicSubscription[]> {
        return this.http.get<TopicSubscription[]>(`${this.API_PATH}?topicId=${topicId}`);
    }
}
