import { firstValueFrom } from 'rxjs';
import { ArticleMockService } from './article-mock.service';

describe('ArticleMockService (jest)', () => {
    it('supports CRUD operations', async () => {
        const service = new ArticleMockService();

        const all1 = await firstValueFrom(service.getAll());
        expect(all1.length).toBeGreaterThan(0);

        const created = await firstValueFrom(
            service.create({ title: 't', content: 'c', authorId: 1, topicId: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' } as any)
        );
        expect(created.id).toBeTruthy();

        const fetched = await firstValueFrom(service.getById(created.id));
        expect(fetched.title).toBe('t');

        const updated = await firstValueFrom(service.update(created.id, { title: 't2' }));
        expect(updated.title).toBe('t2');

        await firstValueFrom(service.delete(created.id));
        const all2 = await firstValueFrom(service.getAll());
        expect(all2.find(a => a.id === created.id)).toBeUndefined();
    });

    it('returns null when updating a missing article', async () => {
        const service = new ArticleMockService();
        const res = await firstValueFrom(service.update(999999, { title: 'x' }));
        expect(res).toBeNull();
    });
});
