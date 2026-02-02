import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Topic } from '../../models/topic.model';

@Injectable({ providedIn: 'root' })
export class TopicService {
    private apiUrl = '/api/topics';

    constructor(private http: HttpClient) { }

    getAll(): Observable<Topic[]> {
        return this.http.get<Topic[]>(this.apiUrl);
    }

    getById(id: number): Observable<Topic> {
        return this.http.get<Topic>(`${this.apiUrl}/${id}`);
    }

    create(topic: Topic): Observable<Topic> {
        return this.http.post<Topic>(this.apiUrl, topic);
    }

    update(id: number, topic: Partial<Topic>): Observable<Topic> {
        return this.http.put<Topic>(`${this.apiUrl}/${id}`, topic);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
