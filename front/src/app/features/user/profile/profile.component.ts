import { SlicePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, finalize, startWith, Subject, switchMap, throwError } from 'rxjs';
import { SUBSCRIPTION_DATASOURCE } from 'src/app/core/services/topic-subscription-datasource.interface';
import { TOPIC_DATASOURCE } from 'src/app/core/services/topic-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';
import { HeaderComponent } from "src/app/shared/header/header.component";
import { User } from 'src/app/core/models/user.model';
import { USER_DATASOURCE } from 'src/app/core/services/user-datasource.interface';
import { CommonComponent } from 'src/app/shared/common-component';
import { toSignal, takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-profile',
  imports: [DynamicFormComponent, FormsModule, SlicePipe, HeaderComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent extends CommonComponent {
  private readonly topicDataSource = inject(TOPIC_DATASOURCE);
  private readonly userDataSource = inject(USER_DATASOURCE);
  private readonly subscriptionDataSource = inject(SUBSCRIPTION_DATASOURCE);
  private readonly refresh$ = new Subject<void>();




  readonly topics = toSignal(
    this.topicDataSource.getAll().pipe(takeUntilDestroyed()),
    { initialValue: [] }
  );

  readonly topicSubscriptions = toSignal(
    this.refresh$.pipe(
      startWith(void 0),
      switchMap(() => this.subscriptionDataSource.getUserTopicSubscriptions()),
      takeUntilDestroyed()
    ),
    { initialValue: [] }
  );

  // Liste des topics abonnés (version réactive)
  readonly myTopics = computed(() => {
    const abonneIds = new Set(this.topicSubscriptions().map(s => s.topicId));
    return this.topics().filter(topic => abonneIds.has(topic.id));
  });


  profileFormElements: FormElement[] = [
    { type: 'text', name: 'username', placeholder: 'Username', required: true },
    { type: 'email', name: 'email', placeholder: 'email@email.fr', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
    {
      type: 'password',
      name: 'password',
      placeholder: 'Mot de passe',
      required: true,
      pattern: '^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$'
    }
  ];


  onFormSubmit(user: User) {
    this.message.set('Mise à jour en cours...');
    this.startSubmit();

    this.userDataSource.update(user.id, user).pipe(
      catchError((error) => {
        this.message.set('Échec de la mise à jour');
        this.error.set(true);
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    ).subscribe({
      next: () => {
        this.message.set('Mise à jour réussie');
      }
    });
  }

  public toggleTopicSubscription(topicId: number) {
    this.subscriptionDataSource.unsubscribeFromTopic(topicId).subscribe(() => {
      this.refresh$.next();
    });
  }
}
