import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ArticleComment } from '../../models/article-comment.model';
import { ArticleCommentDataSource } from '../article-comment-datasource.interface';

@Injectable({ providedIn: 'root' })
export class ArticleCommentService implements ArticleCommentDataSource {
    private readonly apiUrl = '/api/comments';

    constructor(private readonly http: HttpClient) { }

    getAllByArticleId(articleId: number): Observable<ArticleComment[]> {
        return this.http.get<ArticleComment[]>(`${this.apiUrl}?articleId=${articleId}`);
    }

    getAll(): Observable<ArticleComment[]> {
        return this.http.get<ArticleComment[]>(this.apiUrl);
    }

    getById(id: number): Observable<ArticleComment> {
        return this.http.get<ArticleComment>(`${this.apiUrl}/${id}`);
    }

    create(comment: Partial<ArticleComment>): Observable<ArticleComment> {
        return this.http.post<ArticleComment>(this.apiUrl, comment);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
