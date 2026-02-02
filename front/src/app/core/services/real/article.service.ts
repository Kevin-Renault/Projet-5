import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article } from '../../models/article.model';
import { ArticleDataSource } from '../article-datasource.interface';

@Injectable({ providedIn: 'root' })
export class ArticleService implements ArticleDataSource {
    private readonly apiUrl = '/api/articles';

    constructor(private readonly http: HttpClient) { }

    getAll(): Observable<Article[]> {
        return this.http.get<Article[]>(this.apiUrl);
    }

    getById(id: number): Observable<Article> {
        return this.http.get<Article>(`${this.apiUrl}/${id}`);
    }

    create(article: Article): Observable<Article> {
        return this.http.post<Article>(this.apiUrl, article);
    }

    update(id: number, article: Partial<Article>): Observable<Article> {
        return this.http.put<Article>(`${this.apiUrl}/${id}`, article);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    /**
     * Retourne les articles triés par date de création (du plus récent au plus ancien)
     */
    sortByDateDesc(articles: Article[]): Article[] {
        return [...articles].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }

    /**
     * Retourne les articles triés par date de création (du plus ancien au plus récent)
     */
    sortByDateAsc(articles: Article[]): Article[] {
        return [...articles].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }
}
