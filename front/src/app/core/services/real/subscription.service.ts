import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Subscription } from '../../models/subscription.model';
import { SubscriptionDatasource } from '../subscription-datasource.interface';

@Injectable({ providedIn: 'root' })
export class SubscriptionService implements SubscriptionDatasource {
    public readonly API_PATH = '/api/subscriptions';

    constructor(private readonly http: HttpClient) { }

    subscribeOnTopic(userId: number, topicId: number): Observable<Subscription[]> {
        // Remplacer l'URL par celle de l'API réelle
        return this.http.post<Subscription[]>(this.API_PATH, { userId, topicId });
    }

    unsubscribeFromTopic(userId: number, topicId: number): Observable<Subscription[]> {
        // Remplacer l'URL par celle de l'API réelle
        return this.http.delete<Subscription[]>(`${this.API_PATH}?userId=${userId}&topicId=${topicId}`);
    }

    getUserSubscriptions(userId: number): Observable<Subscription[]> {
        return this.http.get<Subscription[]>(`${this.API_PATH}?userId=${userId}`);
    }

    getTopicSubscribers(topicId: number): Observable<Subscription[]> {
        return this.http.get<Subscription[]>(`${this.API_PATH}?topicId=${topicId}`);
    }
}
