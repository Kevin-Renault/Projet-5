import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ArticleComment } from '../../models/article-comment.model';
import { ArticleCommentDataSource } from '../article-comment-datasource.interface';

@Injectable({ providedIn: 'root' })
export class ArticleCommentService implements ArticleCommentDataSource {
    private readonly apiUrl = '/api/comments';

    constructor(private readonly http: HttpClient) { }

    getAllByArticleId(articleId: number): Observable<ArticleComment[]> {
        console.log('[ArticleCommentService] GET', `${this.apiUrl}?articleId=${articleId}`);
        return this.http.get<ArticleComment[]>(`${this.apiUrl}?articleId=${articleId}`)
            .pipe(
                tap(result => console.log('[ArticleCommentService] Response:', result))
            );
    }

    getAll(): Observable<ArticleComment[]> {
        return this.http.get<ArticleComment[]>(this.apiUrl);
    }

    getById(id: number): Observable<ArticleComment> {
        return this.http.get<ArticleComment>(`${this.apiUrl}/${id}`);
    }

    create(comment: Partial<ArticleComment>): Observable<ArticleComment> {
        console.log('[ArticleCommentService] POST', this.apiUrl, comment);
        return this.http.post<ArticleComment>(this.apiUrl, comment)
            .pipe(
                tap(result => console.log('[ArticleCommentService] Response:', result))
            );
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
