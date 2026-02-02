import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Topic } from '../../models/topic.model';
import { TopicDataSource } from '../topic-datasource.interface';

const MOCK_TOPICS: Topic[] = [
    { id: 1, name: 'Java', description: 'Tout sur Java' },
    { id: 2, name: 'Angular', description: 'Tout sur Angular' }
];

@Injectable({ providedIn: 'root' })
export class TopicMockService implements TopicDataSource {
    getAll(): Observable<Topic[]> {
        return of(MOCK_TOPICS);
    }
    getById(id: number): Observable<Topic> {
        return of(MOCK_TOPICS.find(t => t.id === id)!);
    }
    create(topic: Topic): Observable<Topic> {
        return of({ ...topic, id: Date.now() });
    }
    update(id: number, topic: Partial<Topic>): Observable<Topic> {
        return of({ ...MOCK_TOPICS.find(t => t.id === id), ...topic } as Topic);
    }
    delete(id: number): Observable<void> {
        return of(void 0);
    }
}
