import { provideDataSources } from './data-sources.providers';
import { AUTH_DATASOURCE } from '../auth/auth-datasource.interface';
import { AuthMockService } from '../auth/auth-mock.service';
import { AuthService } from '../auth/auth.service';
import { ARTICLE_DATASOURCE } from '../services/article-datasource.interface';
import { ArticleMockService } from '../services/mock/article-mock.service';
import { ArticleService } from '../services/real/article.service';
import { USER_DATASOURCE } from '../services/user-datasource.interface';
import { UserMockService } from '../services/mock/user-mock.service';
import { UserService } from '../services/real/user.service';
import { TOPIC_DATASOURCE } from '../services/topic-datasource.interface';
import { TopicMockService } from '../services/mock/topic-mock.service';
import { TopicService } from '../services/real/topic.service';
import { COMMENT_DATASOURCE } from '../services/article-comment-datasource.interface';
import { ArticleCommentMockService } from '../services/mock/article-comment-mock.service';
import { ArticleCommentService } from '../services/real/article-comment.service';
import { SUBSCRIPTION_DATASOURCE } from '../services/topic-subscription-datasource.interface';
import { TopicSubscriptionMockService } from '../services/mock/topic-subscription-mock.service';
import { TopicSubscriptionService } from '../services/real/subscription.service';

describe('provideDataSources (jest)', () => {
    it('wires mock providers when useMock=true', () => {
        const providers = provideDataSources({ useMock: true }) as any[];
        const byToken = new Map(providers.map(p => [p.provide, p]));

        expect(byToken.get(AUTH_DATASOURCE).useExisting).toBe(AuthMockService);
        expect(byToken.get(ARTICLE_DATASOURCE).useClass).toBe(ArticleMockService);
        expect(byToken.get(USER_DATASOURCE).useClass).toBe(UserMockService);
        expect(byToken.get(TOPIC_DATASOURCE).useClass).toBe(TopicMockService);
        expect(byToken.get(COMMENT_DATASOURCE).useClass).toBe(ArticleCommentMockService);
        expect(byToken.get(SUBSCRIPTION_DATASOURCE).useClass).toBe(TopicSubscriptionMockService);
    });

    it('wires real providers when useMock=false', () => {
        const providers = provideDataSources({ useMock: false }) as any[];
        const byToken = new Map(providers.map(p => [p.provide, p]));

        expect(byToken.get(AUTH_DATASOURCE).useExisting).toBe(AuthService);
        expect(byToken.get(ARTICLE_DATASOURCE).useClass).toBe(ArticleService);
        expect(byToken.get(USER_DATASOURCE).useClass).toBe(UserService);
        expect(byToken.get(TOPIC_DATASOURCE).useClass).toBe(TopicService);
        expect(byToken.get(COMMENT_DATASOURCE).useClass).toBe(ArticleCommentService);
        expect(byToken.get(SUBSCRIPTION_DATASOURCE).useClass).toBe(TopicSubscriptionService);
    });
});
