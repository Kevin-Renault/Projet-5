import { Observable } from 'rxjs';
import { Topic } from '../models/topic.model';

export interface TopicDataSource {
    getAll(): Observable<Topic[]>;
    getById(id: number): Observable<Topic>;
    create(topic: Topic): Observable<Topic>;
    update(id: number, topic: Partial<Topic>): Observable<Topic>;
    delete(id: number): Observable<void>;
}
