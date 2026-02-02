import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Article } from '../../models/article.model';
import { ArticleDataSource } from '../article-datasource.interface';

const MOCK_ARTICLES: Article[] = [
    {
        id: 1,
        title: 'Mock Article 1',
        content: 'Content for mock article 1',
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
        authorId: 1,
        topicId: 1
    },
    {
        id: 2,
        title: 'Mock Article 2',
        content: 'Content for mock article 2',
        createdAt: '2024-01-02T10:00:00Z',
        updatedAt: '2024-01-02T10:00:00Z',
        authorId: 2,
        topicId: 2
    }
];

@Injectable({ providedIn: 'root' })
export class ArticleMockService implements ArticleDataSource {
    getAll(): Observable<Article[]> {
        return of(MOCK_ARTICLES);
    }

    getById(id: number): Observable<Article> {
        return of(MOCK_ARTICLES.find(a => a.id === id)!);
    }

    create(article: Article): Observable<Article> {
        return of({ ...article, id: Date.now() });
    }

    update(id: number, article: Partial<Article>): Observable<Article> {
        return of({ ...MOCK_ARTICLES.find(a => a.id === id), ...article } as Article);
    }

    delete(id: number): Observable<void> {
        return of(void 0);
    }

    sortByDateDesc(articles: Article[]): Article[] {
        return [...articles].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }

    sortByDateAsc(articles: Article[]): Article[] {
        return [...articles].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }
}
