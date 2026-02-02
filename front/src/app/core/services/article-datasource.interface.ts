import { Observable } from 'rxjs';
import { Article } from '../models/article.model';
import { InjectionToken } from '@angular/core';
export const ARTICLE_DATASOURCE = new InjectionToken<ArticleDataSource>('ArticleDataSource');
export interface ArticleDataSource {
    getAll(): Observable<Article[]>;
    getById(id: number): Observable<Article>;
    create(article: Article): Observable<Article>;
    update(id: number, article: Partial<Article>): Observable<Article>;
    delete(id: number): Observable<void>;
    sortByDateDesc(articles: Article[]): Article[];
    sortByDateAsc(articles: Article[]): Article[];
}
