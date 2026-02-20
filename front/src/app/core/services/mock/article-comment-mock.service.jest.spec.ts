import { firstValueFrom } from 'rxjs';
import { ArticleCommentMockService } from './article-comment-mock.service';

describe('ArticleCommentMockService (jest)', () => {
    it('filters by articleId and creates new comments', async () => {
        const service = new ArticleCommentMockService();
        const list = await firstValueFrom(service.getAllByArticleId(1));
        expect(list.every(c => c.articleId === 1)).toBe(true);

        const created = await firstValueFrom(service.create({ articleId: 1, authorId: 1, content: 'x', createdAt: '2024-01-01' } as any));
        expect(created.id).toBeTruthy();
        const list2 = await firstValueFrom(service.getAllByArticleId(1));
        expect(list2.find(c => c.id === created.id)).toBeTruthy();
    });
});
