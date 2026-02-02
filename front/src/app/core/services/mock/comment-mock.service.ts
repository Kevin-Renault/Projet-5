import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Comment } from '../../models/comment.model';
import { CommentDataSource } from '../comment-datasource.interface';

const MOCK_COMMENTS: Comment[] = [
    { id: 1, content: 'Bravo !', createdAt: '2024-01-03T10:00:00Z', authorId: 2, articleId: 1 },
    { id: 2, content: 'Merci pour cet article.', createdAt: '2024-01-04T10:00:00Z', authorId: 1, articleId: 2 }
];

@Injectable({ providedIn: 'root' })
export class CommentMockService implements CommentDataSource {
    getAll(): Observable<Comment[]> {
        return of(MOCK_COMMENTS);
    }
    getById(id: number): Observable<Comment> {
        return of(MOCK_COMMENTS.find(c => c.id === id)!);
    }
    create(comment: Comment): Observable<Comment> {
        return of({ ...comment, id: Date.now() });
    }
    update(id: number, comment: Partial<Comment>): Observable<Comment> {
        return of({ ...MOCK_COMMENTS.find(c => c.id === id), ...comment } as Comment);
    }
    delete(id: number): Observable<void> {
        return of(void 0);
    }
}
