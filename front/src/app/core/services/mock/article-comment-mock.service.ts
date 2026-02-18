import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { ArticleComment } from '../../models/article-comment.model';
import { ArticleCommentDataSource } from '../article-comment-datasource.interface';

@Injectable({ providedIn: 'root' })
export class ArticleCommentMockService implements ArticleCommentDataSource {
    private comments: ArticleComment[] = [
        { id: 1, content: 'Bravo pour cette introduction à Java !', createdAt: '2024-01-03T10:00:00Z', authorId: 2, articleId: 1 },
        { id: 2, content: 'Très clair, merci !', createdAt: '2024-01-04T10:00:00Z', authorId: 3, articleId: 1 },
        { id: 3, content: 'Angular c’est top, bon résumé.', createdAt: '2024-01-05T10:00:00Z', authorId: 1, articleId: 2 },
        { id: 4, content: 'J’aurais aimé plus d’exemples.', createdAt: '2024-01-06T10:00:00Z', authorId: 3, articleId: 2 },
        { id: 5, content: 'Python expliqué simplement, bravo.', createdAt: '2024-01-07T10:00:00Z', authorId: 1, articleId: 3 },
        { id: 6, content: 'Merci pour les astuces SQL.', createdAt: '2024-01-08T10:00:00Z', authorId: 2, articleId: 4 },
        { id: 7, content: 'Super article sur le DevOps !', createdAt: '2024-01-09T10:00:00Z', authorId: 3, articleId: 5 },
        { id: 8, content: 'Docker simplifié, c’est top.', createdAt: '2024-01-10T10:00:00Z', authorId: 1, articleId: 5 },
        { id: 9, content: 'CSS Grid bien expliqué.', createdAt: '2024-01-11T10:00:00Z', authorId: 2, articleId: 6 },
        { id: 10, content: 'Merci pour la clarté.', createdAt: '2024-01-12T10:00:00Z', authorId: 1, articleId: 6 },
        { id: 11, content: 'L’injection de dépendances, enfin compris !', createdAt: '2024-01-13T10:00:00Z', authorId: 3, articleId: 7 },
        { id: 12, content: 'Article utile pour les débutants.', createdAt: '2024-01-14T10:00:00Z', authorId: 1, articleId: 7 },
        { id: 13, content: 'L’OOP en Java, c’est fondamental.', createdAt: '2024-01-15T10:00:00Z', authorId: 2, articleId: 8 },
        { id: 14, content: 'Merci pour la pédagogie.', createdAt: '2024-01-16T10:00:00Z', authorId: 3, articleId: 8 },
        { id: 15, content: 'Super pour réviser les bases.', createdAt: '2024-01-17T10:00:00Z', authorId: 2, articleId: 3 }
    ];

    getAllByArticleId(articleId: number): Observable<ArticleComment[]> {
        return of(this.comments.filter(c => c.articleId === articleId));
    }
    getAll(): Observable<ArticleComment[]> {
        return of(this.comments);
    }
    getById(id: number): Observable<ArticleComment> {
        return of(this.comments.find(c => c.id === id)!);
    }
    create(comment: ArticleComment): Observable<ArticleComment> {
        const newComment = { ...comment, id: Date.now() };
        this.comments.push(newComment);
        return of(newComment);
    }

    update(id: number, comment: Partial<ArticleComment>): Observable<ArticleComment> {
        const idx = this.comments.findIndex(c => c.id === id);
        if (idx === -1) {
            return throwError(() => new Error(`Comment with id ${id} not found`));
        }

        this.comments[idx] = { ...this.comments[idx], ...comment };
        return of(this.comments[idx]);
    }

    delete(id: number): Observable<void> {
        this.comments = this.comments.filter(c => c.id !== id);
        return of(void 0);
    }
}
