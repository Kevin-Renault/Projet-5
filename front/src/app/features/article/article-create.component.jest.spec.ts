import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ARTICLE_DATASOURCE } from '../../core/services/article-datasource.interface';
import { TOPIC_DATASOURCE } from '../../core/services/topic-datasource.interface';
import { AUTH_DATASOURCE } from '../../core/auth/auth-datasource.interface';
import { ArticleCreateComponent } from './article-create.component';

describe('ArticleCreateComponent (integration jest)', () => {
    it('creates article and navigates back to list', async () => {
        const navigate = jest.fn();
        const create = jest.fn(() => of({ id: 1 } as any));

        TestBed.configureTestingModule({
            imports: [ArticleCreateComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll: () => of([{ id: 1, name: 'T' }] as any) } },
                { provide: ARTICLE_DATASOURCE, useValue: { create } },
                { provide: Router, useValue: { navigate } },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(ArticleCreateComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit({ topicId: '1', title: 't', content: 'c' });

        expect(create).toHaveBeenCalled();
        expect(component.isLoading()).toBe(false);
        expect(navigate).toHaveBeenCalledWith(['/articles']);
        expect(component.message()).toContain('réussie');
    });

    it('sets error/message on create failure', async () => {
        const create = jest.fn(() => throwError(() => new Error('nope')));

        TestBed.configureTestingModule({
            imports: [ArticleCreateComponent],
            providers: [
                { provide: TOPIC_DATASOURCE, useValue: { getAll: () => of([]) } },
                { provide: ARTICLE_DATASOURCE, useValue: { create } },
                { provide: Router, useValue: { navigate: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(ArticleCreateComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit({ topicId: '1', title: 't', content: 'c' });
        expect(component.error()).toBe(true);
        expect(component.message()).toContain('Erreur');
        expect(component.isLoading()).toBe(false);
    });
});
