import { Observable } from 'rxjs';
import { ArticleComment } from '../models/article-comment.model';
import { InjectionToken } from '@angular/core';

export const COMMENT_DATASOURCE = new InjectionToken<ArticleCommentDataSource>('ArticleCommentDataSource');
export interface ArticleCommentDataSource {
    getAll(): Observable<ArticleComment[]>;
    getAllByArticleId(articleId: number): Observable<ArticleComment[]>;
    getById(id: number): Observable<ArticleComment>;
    create(comment: Partial<ArticleComment>): Observable<ArticleComment>;
}
