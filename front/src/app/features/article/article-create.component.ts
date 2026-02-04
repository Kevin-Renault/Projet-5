import { Component, Inject, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { Article } from 'src/app/core/models/article.model';
import { Topic } from 'src/app/core/models/topic.model';
import { ARTICLE_DATASOURCE, ArticleDataSource } from 'src/app/core/services/article-datasource.interface';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';

@Component({
  selector: 'app-article-create',
  imports: [DynamicFormComponent],
  templateUrl: './article-create.component.html',
  styleUrls: ['./article-create.component.scss']
})
export class ArticleCreateComponent implements OnDestroy {
  articleFormElements: FormElement[] = [
    {
      type: 'select', name: 'topicId', placeholder: 'Sélectionner un thème', required: true, options: []
    },
    { type: 'text', name: 'title', placeholder: 'Titre de l\'article', required: true },
    { type: 'textarea', name: 'content', placeholder: 'Contenu de l\'article', required: true }
  ];


  constructor(@Inject(TOPIC_DATASOURCE) private readonly topicDataSource: TopicDataSource,
    @Inject(ARTICLE_DATASOURCE) private readonly articleDataSource: ArticleDataSource,
    private readonly router: Router) {
    this.topicDataSource.getAll().subscribe((topics: Topic[]) => {
      this.articleFormElements[0].options = topics.map(t => ({
        value: t.id + "",
        label: t.name
      }));
    });
  }

  private readonly destroy$ = new Subject<void>();
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onFormSubmit(values: Partial<Article>): void {

    values.createdAt = new Date().toISOString();
    values.updatedAt = new Date().toISOString();
    //TODO: Remplacer par l'ID de l'utilisateur connecté
    values.authorId = 1; // Utilisateur fictif
    this.articleDataSource.create(values as Article).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => { this.router.navigate(['/articles']); },
      error: () => alert('Erreur lors de la création de l\'article'),
    });
  }
}
