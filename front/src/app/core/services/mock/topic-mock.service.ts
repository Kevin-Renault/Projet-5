import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Topic } from '../../models/topic.model';
import { TopicDataSource } from '../topic-datasource.interface';

const MOCK_TOPICS: Topic[] = [
    {
        id: 1,
        name: 'Java',
        description: 'Explorez le langage Java en profondeur : syntaxe, programmation orientée objet, gestion de la mémoire, collections, flux, multithreading, et bonnes pratiques pour le développement d’applications robustes côté serveur et desktop.'
    },
    {
        id: 2,
        name: 'Angular',
        description: 'Maîtrisez Angular, le framework moderne pour le développement d’applications web dynamiques : architecture modulaire, composants standalone, gestion des routes, services, RxJS, Material Design, et optimisation des performances.'
    },
    {
        id: 3,
        name: 'Python',
        description: 'Découvrez Python et son vaste écosystème : syntaxe claire, programmation orientée objet et fonctionnelle, gestion des modules, data science, automatisation, web (Django/Flask), et outils pour l’intelligence artificielle.'
    },
    {
        id: 4,
        name: 'Web',
        description: 'Plongez dans le développement web : HTML5, CSS3, JavaScript moderne, accessibilité, responsive design, frameworks front-end, optimisation SEO, performances, et bonnes pratiques pour des sites interactifs et accessibles.'
    },
    {
        id: 5,
        name: 'DevOps',
        description: 'Approfondissez DevOps : intégration et déploiement continus (CI/CD), conteneurisation avec Docker, orchestration Kubernetes, monitoring, cloud computing, automatisation des workflows, et sécurité des infrastructures.'
    },
    {
        id: 6,
        name: 'Base de données',
        description: 'Maîtrisez les bases de données : SQL avancé, conception de schémas, optimisation des requêtes, transactions, indexation, NoSQL, migration de données, sécurité, et gestion des performances pour des applications évolutives.'
    }
];

@Injectable({ providedIn: 'root' })
export class TopicMockService implements TopicDataSource {
    getAll(): Observable<Topic[]> {
        return of(MOCK_TOPICS);
    }
    getById(id: number): Observable<Topic> {
        return of(MOCK_TOPICS.find(t => t.id === id)!);
    }
    create(topic: Topic): Observable<Topic> {
        return of({ ...topic, id: Date.now() });
    }
    update(id: number, topic: Partial<Topic>): Observable<Topic> {
        return of({ ...MOCK_TOPICS.find(t => t.id === id), ...topic } as Topic);
    }
    delete(id: number): Observable<void> {
        return of(void 0);
    }
}
