import { Component, Inject } from '@angular/core';
import { SlicePipe, AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/internal/Observable';
import { Topic } from 'src/app/core/models/topic.model';
import { TopicSubscription } from 'src/app/core/models/topic-subscription.model';
import { SUBSCRIPTION_DATASOURCE, TopicSubscriptionDatasource } from 'src/app/core/services/topic-subscription-datasource.interface';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';
import { HeaderComponent } from "src/app/shared/header/header.component";

@Component({
  selector: 'app-topic',
  imports: [FormsModule, SlicePipe, AsyncPipe, HeaderComponent],
  templateUrl: './topic.component.html',
  styleUrls: ['./topic.component.scss']
})
export class TopicComponent {

  isSubscribed(topicId: number): boolean {
    return this.subscribedTopicIds.has(topicId);
  }



  topics$: Observable<Topic[]> | null = null;
  subscription$: Observable<TopicSubscription[]> | null = null;
  userId: number;
  subscribedTopicIds = new Set<number>();

  constructor(
    @Inject(TOPIC_DATASOURCE) private readonly topicDataSource: TopicDataSource,
    @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
    @Inject(SUBSCRIPTION_DATASOURCE) private readonly subscriptionDataSource: TopicSubscriptionDatasource,
    private readonly router: Router
  ) {
    this.topics$ = this.topicDataSource.getAll();
    const userId = this.authDataSource.getCurrentUserId();
    if (typeof userId === 'number') {
      this.userId = userId;
    } else {
      alert('No authenticated user. Cannot load profile.');
      // Optionally redirect to login or handle error
      this.userId = -1;
    }
    this.subscription$ = this.subscriptionDataSource.getUserTopicSubscriptions(this.userId);
    this.subscription$.subscribe(subs => {
      this.subscribedTopicIds = new Set(subs.map(s => s.topicId));
    });
  }


  public toggleTopicSubscription(topicId: number) {

    if (this.isSubscribed(topicId)) {
      this.subscriptionDataSource.unsubscribeFromTopic(this.userId, topicId).subscribe(() => {
        this.refreshTopicSubscriptions();
      });
    } else {
      this.subscriptionDataSource.subscribeOnTopic(this.userId, topicId).subscribe(() => {
        this.refreshTopicSubscriptions();
      });
    }
  }

  private refreshTopicSubscriptions() {
    this.subscription$ = this.subscriptionDataSource.getUserTopicSubscriptions(this.userId);
    this.subscription$.subscribe(subs => {
      this.subscribedTopicIds = new Set(subs.map(s => s.topicId));
    });
  }
}
