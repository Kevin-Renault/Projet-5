import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { ARTICLE_DATASOURCE } from '../../core/services/article-datasource.interface';
import { USER_DATASOURCE } from '../../core/services/user-datasource.interface';
import { AUTH_DATASOURCE } from '../../core/auth/auth-datasource.interface';
import { ArticleListComponent } from './article-list.component';

describe('ArticleListComponent (integration jest)', () => {
    it('navigates and toggles sort order', async () => {
        const navigate = jest.fn();

        TestBed.configureTestingModule({
            imports: [ArticleListComponent],
            providers: [
                {
                    provide: ARTICLE_DATASOURCE,
                    useValue: {
                        getAll: () => of([
                            {
                                id: 1,
                                authorId: 1,
                                title: 'Hello',
                                content: 'Some content',
                                createdAt: '2024-01-01T00:00:00.000Z',
                            },
                        ] as any),
                    },
                },
                { provide: USER_DATASOURCE, useValue: { getAll: () => of([{ id: 1, username: 'bob' }] as any) } },
                { provide: Router, useValue: { navigate } },
                { provide: AUTH_DATASOURCE, useValue: { isAuthenticated$: () => (() => true) } },
            ],
        });

        const fixture = TestBed.createComponent(ArticleListComponent);
        const component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();

        expect(component.authorById(1)).toBe('bob');
        expect(component.authorById(999)).toBe('Inconnu');

        const before = component.sortOrder();
        component.toggleSortOrder();
        expect(component.sortOrder()).not.toBe(before);

        component.createArticle();
        expect(navigate).toHaveBeenCalledWith(['/articles/create']);

        component.openArticleComments(10);
        expect(navigate).toHaveBeenCalledWith(['/articles', 10, 'comment']);
    });
});
