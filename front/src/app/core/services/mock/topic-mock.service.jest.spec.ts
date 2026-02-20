import { firstValueFrom } from 'rxjs';
import { TopicMockService } from './topic-mock.service';

describe('TopicMockService (jest)', () => {
    it('returns topics and a topic by id', async () => {
        const service = new TopicMockService();
        const topics = await firstValueFrom(service.getAll());
        expect(topics.length).toBeGreaterThan(0);

        const t1 = await firstValueFrom(service.getById(1));
        expect(t1.id).toBe(1);
    });
});
