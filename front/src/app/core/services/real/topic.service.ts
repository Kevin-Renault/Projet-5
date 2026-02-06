import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Topic } from '../../models/topic.model';

@Injectable({ providedIn: 'root' })
export class TopicService {
    private readonly apiUrl = '/api/topics';

    constructor(private readonly http: HttpClient) { }

    getAll(): Observable<Topic[]> {
        return this.http.get<Topic[]>(this.apiUrl);
    }

    getById(id: number): Observable<Topic> {
        return this.http.get<Topic>(`${this.apiUrl}/${id}`);
    }
}
