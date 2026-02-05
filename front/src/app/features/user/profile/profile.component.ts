import { SlicePipe, AsyncPipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { combineLatest, map, Observable } from 'rxjs';
import { Topic } from 'src/app/core/models/topic.model';
import { SUBSCRIPTION_DATASOURCE, TopicSubscriptionDatasource } from 'src/app/core/services/topic-subscription-datasource.interface';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';

@Component({
  selector: 'app-profile',
  imports: [DynamicFormComponent, FormsModule, SlicePipe, AsyncPipe],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {
  profileFormElements: FormElement[] = [
    { type: 'text', name: 'name', placeholder: 'Username', required: true },
    { type: 'email', name: 'email', placeholder: 'email@email.fr', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
    {
      type: 'password',
      name: 'password',
      placeholder: 'Mot de passe',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];



  onFormSubmit(values: any) {
    alert('Form submitted: ' + JSON.stringify(values, null, 2));
  }

  userId: number;
  myTopics$: Observable<Topic[]> | null = null;

  constructor(
    @Inject(TOPIC_DATASOURCE) private readonly topicDataSource: TopicDataSource,
    @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
    @Inject(SUBSCRIPTION_DATASOURCE) private readonly subscriptionDataSource: TopicSubscriptionDatasource
  ) {


    const userId = this.authDataSource.getCurrentUserId();
    if (typeof userId === 'number') {
      this.userId = userId;
    } else {
      alert('No authenticated user. Cannot load profile.');
      // Optionally redirect to login or handle error
      this.userId = -1;
    }
    this.myTopics$ = combineLatest([
      this.topicDataSource.getAll(),
      this.subscriptionDataSource.getUserTopicSubscriptions(this.userId)
    ]).pipe(
      map(([topics, subs]) => {
        const abonneIds = new Set(subs.map(s => s.topicId));
        return topics.filter(topic => abonneIds.has(topic.id));
      })
    );
  }



  public toggleTopicSubscription(topicId: number) {
    this.subscriptionDataSource.unsubscribeFromTopic(this.userId, topicId).subscribe(() => {
      this.refreshTopicSubscriptions();
    });
  }

  private refreshTopicSubscriptions() {

    // Met à jour la liste des topics auxquels l'utilisateur est abonné
    this.myTopics$ = combineLatest([
      this.topicDataSource.getAll(),
      this.subscriptionDataSource.getUserTopicSubscriptions(this.userId)
    ]).pipe(
      map(([topics, subs]) => {
        const abonneIds = new Set(subs.map(s => s.topicId));
        return topics.filter(topic => abonneIds.has(topic.id));
      })
    );
  }
}
