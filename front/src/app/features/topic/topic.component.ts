import { Component, computed, inject } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TOPIC_DATASOURCE } from 'src/app/core/services/topic-datasource.interface';
import { SUBSCRIPTION_DATASOURCE } from 'src/app/core/services/topic-subscription-datasource.interface';
import { HeaderComponent } from "src/app/shared/header/header.component";
import { CommonComponent } from 'src/app/shared/common-component';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Subject } from 'rxjs/internal/Subject';
import { finalize, startWith, switchMap } from 'rxjs';

@Component({
  selector: 'app-topic',
  imports: [FormsModule, SlicePipe, HeaderComponent],
  templateUrl: './topic.component.html',
  styleUrls: ['./topic.component.scss']
})
export class TopicComponent extends CommonComponent {

  private readonly topicDataSource = inject(TOPIC_DATASOURCE);
  private readonly subscriptionDataSource = inject(SUBSCRIPTION_DATASOURCE);

  private readonly refresh$ = new Subject<void>();


  protected override computeIsPageLoading(): boolean {
    // Logique personnalisée pour la classe enfant
    return this.topics().length == 0; // Exemple : ne vérifie que article$
  }

  readonly topicSubscriptions = toSignal(
    this.refresh$.pipe(
      startWith(void 0),
      switchMap(() => this.subscriptionDataSource.getUserTopicSubscriptions()),
      takeUntilDestroyed()
    ),
    { initialValue: [] }
  );

  readonly subscribedTopicIds = computed(() =>
    new Set(this.topicSubscriptions().map(sub => sub.topicId))
  );

  isSubscribed(topicId: number): boolean {
    return this.subscribedTopicIds().has(topicId);
  }

  readonly topics = toSignal(
    this.topicDataSource.getAll(),
    { initialValue: [] }
  );

  public toggleTopicSubscription(topicId: number): void {
    this.startSubmit(); // Active l'état de chargement

    const action$ = this.isSubscribed(topicId)
      ? this.subscriptionDataSource.unsubscribeFromTopic(topicId)
      : this.subscriptionDataSource.subscribeOnTopic(topicId);

    action$.pipe(
      finalize(() => this.isLoading.set(false)) // Désactive le chargement dans tous les cas
    ).subscribe({
      next: () => {
        // ✅ Rafraîchit UNIQUEMENT en cas de succès
        this.refresh$.next();
        this.message.set(this.isSubscribed(topicId)
          ? 'Désabonnement réussi'
          : 'Abonnement réussi');
      },
      error: (err) => {
        this.error.set(true);
        this.message.set(`Erreur: ${err.message}`);
      }
    });
  }
}

