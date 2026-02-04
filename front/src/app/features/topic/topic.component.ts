import { Component, Inject } from '@angular/core';
import { SlicePipe, AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/internal/Observable';
import { Topic } from 'src/app/core/models/topic.model';
import { Subscription } from 'src/app/core/models/subscription.model';
import { SUBSCRIPTION_DATASOURCE, SubscriptionDatasource } from 'src/app/core/services/subscription-datasource.interface';

@Component({
  selector: 'app-topic',
  imports: [FormsModule, SlicePipe, AsyncPipe],
  templateUrl: './topic.component.html',
  styleUrls: ['./topic.component.scss']
})
export class TopicComponent {

  isSubscribed(topicId: number): boolean {
    return this.subscribedTopicIds.has(topicId);
  }



  topics$: Observable<Topic[]> | null = null;
  subscription$: Observable<Subscription[]> | null = null;
  userId: number;
  subscribedTopicIds = new Set<number>();

  constructor(
    @Inject(TOPIC_DATASOURCE) private readonly topicDataSource: TopicDataSource,
    @Inject(SUBSCRIPTION_DATASOURCE) private readonly subscriptionDataSource: SubscriptionDatasource,
    private readonly router: Router
  ) {
    this.topics$ = this.topicDataSource.getAll();
    this.userId = 1; // TODO: Remplacer par l'ID de l'utilisateur connecté
    this.subscription$ = this.subscriptionDataSource.getUserSubscriptions(this.userId);
    this.subscription$.subscribe(subs => {
      this.subscribedTopicIds = new Set(subs.map(s => s.topicId));
    });
  }


  public toggleSubscription(topicId: number) {

    if (this.isSubscribed(topicId)) {
      this.subscriptionDataSource.unsubscribeFromTopic(this.userId, topicId).subscribe(() => {
        this.refreshSubscriptions();
      });
    } else {
      this.subscriptionDataSource.subscribeOnTopic(this.userId, topicId).subscribe(() => {
        this.refreshSubscriptions();
      });
    }
  }

  private refreshSubscriptions() {
    this.subscription$ = this.subscriptionDataSource.getUserSubscriptions(this.userId);
    this.subscription$.subscribe(subs => {
      this.subscribedTopicIds = new Set(subs.map(s => s.topicId));
    });
  }
}
