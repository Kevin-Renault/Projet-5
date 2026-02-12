import { Component, Inject, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';
import { Article } from 'src/app/core/models/article.model';
import { Topic } from 'src/app/core/models/topic.model';
import { ARTICLE_DATASOURCE, ArticleDataSource } from 'src/app/core/services/article-datasource.interface';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';
import { HeaderComponent } from "src/app/shared/header/header.component";

@Component({
  selector: 'app-article-create',
  imports: [DynamicFormComponent, HeaderComponent],
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
    @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
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

    const authorId = this.authDataSource.getCurrentUserId();
    if (typeof authorId === 'number') {
      values.authorId = authorId;
      this.articleDataSource.create(values as Article).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: () => { this.router.navigate(['/articles']); },
        error: () => alert('Erreur lors de la création de l\'article'),
      });
    } else {
      alert('No authenticated user. Cannot create article.');
    }
  }
}
