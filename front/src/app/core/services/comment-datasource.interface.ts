import { Observable } from 'rxjs';
import { Comment } from '../models/comment.model';

export interface CommentDataSource {
    getAll(): Observable<Comment[]>;
    getById(id: number): Observable<Comment>;
    create(comment: Comment): Observable<Comment>;
    update(id: number, comment: Partial<Comment>): Observable<Comment>;
    delete(id: number): Observable<void>;
}
