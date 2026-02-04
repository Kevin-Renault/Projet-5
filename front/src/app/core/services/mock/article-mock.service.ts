import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Article } from '../../models/article.model';
import { ArticleDataSource } from '../article-datasource.interface';

@Injectable({ providedIn: 'root' })
export class ArticleMockService implements ArticleDataSource {
    private articles: Article[] = [
        {
            id: 1,
            title: 'Introduction à Java',
            content: 'Découvrez les bases du langage Java, sa syntaxe et ses principaux concepts orientés objet.',
            createdAt: '2024-01-01T10:00:00Z',
            updatedAt: '2024-01-01T10:00:00Z',
            authorId: 1, // alice
            topicId: 1 // Java
        },
        {
            id: 2,
            title: 'Angular : Premiers pas',
            content: 'Comment démarrer un projet Angular, comprendre la structure et créer ses premiers composants.',
            createdAt: '2024-01-02T10:00:00Z',
            updatedAt: '2024-01-02T10:00:00Z',
            authorId: 2, // bob
            topicId: 2 // Angular
        },
        {
            id: 3,
            title: 'Les bases de Python',
            content: 'Variables, boucles, fonctions : tout pour bien débuter en Python.',
            createdAt: '2024-01-03T10:00:00Z',
            updatedAt: '2024-01-03T10:00:00Z',
            authorId: 3, // charlie
            topicId: 3 // Python
        },
        {
            id: 4,
            title: 'Optimiser vos requêtes SQL',
            content: 'Techniques et astuces pour écrire des requêtes SQL performantes et éviter les pièges courants.',
            createdAt: '2024-01-04T10:00:00Z',
            updatedAt: '2024-01-04T10:00:00Z',
            authorId: 1, // alice
            topicId: 6 // Base de données
        },
        {
            id: 5,
            title: 'Déploiement continu avec Docker',
            content: 'Mettez en place une chaîne CI/CD moderne grâce à Docker et aux outils DevOps.',
            createdAt: '2024-01-05T10:00:00Z',
            updatedAt: '2024-01-05T10:00:00Z',
            authorId: 2, // bob
            topicId: 5 // DevOps
        },
        {
            id: 6,
            title: 'CSS Grid et Flexbox',
            content: 'Maîtrisez la mise en page moderne avec CSS Grid et Flexbox pour des interfaces web réactives.',
            createdAt: '2024-01-06T10:00:00Z',
            updatedAt: '2024-01-06T10:00:00Z',
            authorId: 3, // charlie
            topicId: 4 // Web
        },
        {
            id: 7,
            title: 'Services et injection de dépendances dans Angular',
            content: 'Comprendre comment fonctionnent les services et l’injection de dépendances dans Angular.',
            createdAt: '2024-01-07T10:00:00Z',
            updatedAt: '2024-01-07T10:00:00Z',
            authorId: 2, // bob
            topicId: 2 // Angular
        },
        {
            id: 8,
            title: 'Programmation orientée objet en Java',
            content: 'Les principes de l’OOP appliqués à Java : héritage, encapsulation, polymorphisme.',
            createdAt: '2024-01-08T10:00:00Z',
            updatedAt: '2024-01-08T10:00:00Z',
            authorId: 1, // alice
            topicId: 1 // Java
        }
    ];

    getAll(): Observable<Article[]> {
        return of(this.articles);
    }

    getById(id: number): Observable<Article> {
        return of(this.articles.find(a => a.id === id)!);
    }

    create(article: Article): Observable<Article> {
        const newArticle = { ...article, id: Date.now() };
        this.articles.push(newArticle);
        return of(newArticle);
    }

    update(id: number, article: Partial<Article>): Observable<Article> {
        const idx = this.articles.findIndex(a => a.id === id);
        if (idx !== -1) {
            this.articles[idx] = { ...this.articles[idx], ...article };
            return of(this.articles[idx]);
        }
        return of(null as any);
    }

    delete(id: number): Observable<void> {
        this.articles = this.articles.filter(a => a.id !== id);
        return of(void 0);
    }

    sortByDateDesc(articles: Article[]): Article[] {
        return [...articles].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }

    sortByDateAsc(articles: Article[]): Article[] {
        return [...articles].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }
}
