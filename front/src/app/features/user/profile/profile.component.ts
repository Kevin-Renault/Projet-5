import { SlicePipe, AsyncPipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { combineLatest, map, Observable } from 'rxjs';
import { Subscription } from 'src/app/core/models/subscription.model';
import { Topic } from 'src/app/core/models/topic.model';
import { SUBSCRIPTION_DATASOURCE, SubscriptionDatasource } from 'src/app/core/services/subscription-datasource.interface';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

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
    @Inject(SUBSCRIPTION_DATASOURCE) private readonly subscriptionDataSource: SubscriptionDatasource,
    private readonly router: Router
  ) {

    this.userId = 1; // TODO: Remplacer par l'ID de l'utilisateur connecté
    this.myTopics$ = combineLatest([
      this.topicDataSource.getAll(),
      this.subscriptionDataSource.getUserSubscriptions(this.userId)
    ]).pipe(
      map(([topics, subs]) => {
        const abonneIds = new Set(subs.map(s => s.topicId));
        return topics.filter(topic => abonneIds.has(topic.id));
      })
    );
  }



  public toggleSubscription(topicId: number) {
    this.subscriptionDataSource.unsubscribeFromTopic(this.userId, topicId).subscribe(() => {
      this.refreshSubscriptions();
    });
  }

  private refreshSubscriptions() {

    // Met à jour la liste des topics auxquels l'utilisateur est abonné
    this.myTopics$ = combineLatest([
      this.topicDataSource.getAll(),
      this.subscriptionDataSource.getUserSubscriptions(this.userId)
    ]).pipe(
      map(([topics, subs]) => {
        const abonneIds = new Set(subs.map(s => s.topicId));
        return topics.filter(topic => abonneIds.has(topic.id));
      })
    );
  }
}
