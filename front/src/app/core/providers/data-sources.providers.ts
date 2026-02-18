import { Provider } from '@angular/core';

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

export type DataSourceProvidersOptions = {
    useMock: boolean;
};

/**
 * Centralise le câblage DI des datasources (mock vs real).
 *
 * Usage app:
 *  providers: [ ...provideDataSources({ useMock: environment.useMock }) ]
 *
 * Usage TU:
 *  TestBed.configureTestingModule({ providers: [ ...provideDataSources({ useMock: true }) ] })
 */
export function provideDataSources(options: DataSourceProvidersOptions): Provider[] {
    return [
        {
            provide: AUTH_DATASOURCE,
            // useExisting évite d'avoir 2 instances d'AuthService/AuthMockService
            // (une via providedIn: 'root' + une via le token).
            useExisting: options.useMock ? AuthMockService : AuthService,
        },
        {
            provide: ARTICLE_DATASOURCE,
            useClass: options.useMock ? ArticleMockService : ArticleService,
        },
        {
            provide: USER_DATASOURCE,
            useClass: options.useMock ? UserMockService : UserService,
        },
        {
            provide: TOPIC_DATASOURCE,
            useClass: options.useMock ? TopicMockService : TopicService,
        },
        {
            provide: COMMENT_DATASOURCE,
            useClass: options.useMock ? ArticleCommentMockService : ArticleCommentService,
        },
        {
            provide: SUBSCRIPTION_DATASOURCE,
            useClass: options.useMock ? TopicSubscriptionMockService : TopicSubscriptionService,
        },
    ];
}
