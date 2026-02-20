import { convertToParamMap, Router } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom, of, throwError } from 'rxjs';
import { ARTICLE_DATASOURCE } from '../../core/services/article-datasource.interface';
import { COMMENT_DATASOURCE } from '../../core/services/article-comment-datasource.interface';
import { TOPIC_DATASOURCE } from '../../core/services/topic-datasource.interface';
import { USER_DATASOURCE } from '../../core/services/user-datasource.interface';
import { AUTH_DATASOURCE } from '../../core/auth/auth-datasource.interface';
import { ArticleCommentComponent } from './article-comment.component';

describe('ArticleCommentComponent (integration jest)', () => {
    it('renders with route id and caches author/topic lookups', async () => {
        const userGetById = jest.fn((id: number) => of({ id, username: `u${id}` } as any));
        const topicGetById = jest.fn((id: number) => of({ id, name: `t${id}` } as any));

        TestBed.configureTestingModule({
            imports: [ArticleCommentComponent],
            providers: [
                { provide: Router, useValue: { navigate: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { logout: jest.fn(), isAuthenticated$: () => (() => true), getCurrentUser: () => ({ id: 1 }) } },
                { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
                {
                    provide: ARTICLE_DATASOURCE,
                    useValue: {
                        getById: (id: number) =>
                            of({ id, title: 'A', content: 'C', createdAt: '2024-01-01T00:00:00.000Z', authorId: 1, topicId: 2 } as any),
                    },
                },
                { provide: COMMENT_DATASOURCE, useValue: { getAllByArticleId: () => of([]), create: jest.fn(() => of({ id: 1 } as any)) } },
                { provide: USER_DATASOURCE, useValue: { getById: userGetById, getAll: () => of([]) } },
                { provide: TOPIC_DATASOURCE, useValue: { getById: topicGetById, getAll: () => of([]) } },
            ],
        });

        const fixture = TestBed.createComponent(ArticleCommentComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        await firstValueFrom(component.authorById(1));
        await firstValueFrom(component.authorById(1));
        expect(userGetById).toHaveBeenCalledTimes(1);

        await firstValueFrom(component.topicById(2));
        await firstValueFrom(component.topicById(2));
        expect(topicGetById).toHaveBeenCalledTimes(1);
    });

    it('submits a comment and handles failures', async () => {
        const create = jest.fn(() => of({ id: 10 } as any));

        TestBed.configureTestingModule({
            imports: [ArticleCommentComponent],
            providers: [
                { provide: Router, useValue: { navigate: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { logout: jest.fn(), isAuthenticated$: () => (() => true), getCurrentUser: () => ({ id: 1 }) } },
                { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
                { provide: ARTICLE_DATASOURCE, useValue: { getById: () => of({ id: 1, title: 'A', content: 'C', createdAt: '2024-01-01', authorId: 1, topicId: 1 } as any) } },
                { provide: COMMENT_DATASOURCE, useValue: { getAllByArticleId: () => of([]), create } },
                { provide: USER_DATASOURCE, useValue: { getById: () => of({ id: 1, username: 'u1' } as any), getAll: () => of([]) } },
                { provide: TOPIC_DATASOURCE, useValue: { getById: () => of({ id: 1, name: 't1' } as any), getAll: () => of([]) } },
            ],
        });

        const fixture = TestBed.createComponent(ArticleCommentComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        component.onFormSubmit({ content: 'hello' });
        expect(create).toHaveBeenCalled();
        expect(component.message()).toContain('réussie');
        expect(component.isLoading()).toBe(false);

        // failure path
        const create2 = jest.fn(() => throwError(() => new Error('nope')));
        TestBed.resetTestingModule();
        TestBed.configureTestingModule({
            imports: [ArticleCommentComponent],
            providers: [
                { provide: Router, useValue: { navigate: jest.fn() } },
                { provide: AUTH_DATASOURCE, useValue: { logout: jest.fn(), isAuthenticated$: () => (() => true), getCurrentUser: () => ({ id: 1 }) } },
                { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
                { provide: ARTICLE_DATASOURCE, useValue: { getById: () => of({ id: 1, title: 'A', content: 'C', createdAt: '2024-01-01', authorId: 1, topicId: 1 } as any) } },
                { provide: COMMENT_DATASOURCE, useValue: { getAllByArticleId: () => of([]), create: create2 } },
                { provide: USER_DATASOURCE, useValue: { getById: () => of({ id: 1, username: 'u1' } as any), getAll: () => of([]) } },
                { provide: TOPIC_DATASOURCE, useValue: { getById: () => of({ id: 1, name: 't1' } as any), getAll: () => of([]) } },
            ],
        });

        const fixture2 = TestBed.createComponent(ArticleCommentComponent);
        const component2 = fixture2.componentInstance;
        fixture2.detectChanges();
        await fixture2.whenStable();

        component2.onFormSubmit({ content: 'oops' });
        expect(component2.error()).toBe(true);
        expect(component2.message()).toContain('Erreur');
        expect(component2.isLoading()).toBe(false);
    });
});
