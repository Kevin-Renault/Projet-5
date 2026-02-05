import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app-routing.module';
import { environment } from './environments/environment';
import { enableProdMode } from '@angular/core';
import { ARTICLE_DATASOURCE } from './app/core/services/article-datasource.interface';
import { ArticleMockService } from './app/core/services/mock/article-mock.service';
import { ArticleService } from './app/core/services/real/article.service';
import { UserService } from './app/core/services/real/user.service';
import { UserMockService } from './app/core/services/mock/user-mock.service';
import { USER_DATASOURCE } from './app/core/services/user-datasource.interface';
import { TopicService } from './app/core/services/real/topic.service';
import { TOPIC_DATASOURCE } from './app/core/services/topic-datasource.interface';
import { TopicMockService } from './app/core/services/mock/topic-mock.service';
import { provideHttpClient } from '@angular/common/http';
import { COMMENT_DATASOURCE } from './app/core/services/article-comment-datasource.interface';
import { ArticleCommentService } from './app/core/services/real/article-comment.service';
import { ArticleCommentMockService } from './app/core/services/mock/article-comment-mock.service';
import { SUBSCRIPTION_DATASOURCE } from './app/core/services/topic-subscription-datasource.interface';
import { TopicSubscriptionService } from './app/core/services/real/subscription.service';
import { TopicSubscriptionMockService } from './app/core/services/mock/topic-subscription-mock.service';
if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes),
  provideHttpClient(),
  {
    provide: ARTICLE_DATASOURCE,
    useClass: environment.useMock ? ArticleMockService : ArticleService
  }, {
    provide: USER_DATASOURCE,
    useClass: environment.useMock ? UserMockService : UserService
  }, {
    provide: TOPIC_DATASOURCE,
    useClass: environment.useMock ? TopicMockService : TopicService
  }, {
    provide: COMMENT_DATASOURCE,
    useClass: environment.useMock ? ArticleCommentMockService : ArticleCommentService
  }, {
    provide: SUBSCRIPTION_DATASOURCE,
    useClass: environment.useMock ? TopicSubscriptionMockService : TopicSubscriptionService
  }]
}).catch(err => console.error(err));
