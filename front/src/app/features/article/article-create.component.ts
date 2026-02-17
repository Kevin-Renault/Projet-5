import { Component, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { catchError, finalize, throwError } from 'rxjs';
import { AUTH_DATASOURCE } from 'src/app/core/auth/auth-datasource.interface';
import { Article } from 'src/app/core/models/article.model';
import { ARTICLE_DATASOURCE } from 'src/app/core/services/article-datasource.interface';
import { TOPIC_DATASOURCE } from 'src/app/core/services/topic-datasource.interface';
import { CommonComponent } from 'src/app/shared/common-component';
import { FormElement, DynamicFormComponent } from 'src/app/shared/form/dynamic-form.component';
import { HeaderComponent } from "src/app/shared/header/header.component";

@Component({
  selector: 'app-article-create',
  imports: [DynamicFormComponent, HeaderComponent],
  templateUrl: './article-create.component.html',
  styleUrls: ['./article-create.component.scss']
})
export class ArticleCreateComponent extends CommonComponent {

  private readonly topicDataSource = inject(TOPIC_DATASOURCE);
  private readonly articleDataSource = inject(ARTICLE_DATASOURCE);
  private readonly router = inject(Router);

  protected override computeIsPageLoading(): boolean {
    return this.topics().length == 0;
  }

  constructor() {
    super();
    // Met à jour les options du formulaire quand topicOptions change
    effect(() => {
      this.articleFormElements.update(elements => {
        const updatedElements = [...elements];
        updatedElements[0] = {
          ...updatedElements[0],
          options: this.topicOptions() // ✅ Met à jour les options
        };
        return updatedElements;
      });
    });
  }

  readonly articleFormElements = signal<FormElement[]>([
    {
      type: 'select',
      name: 'topicId',
      placeholder: 'Sélectionner un thème',
      required: true,
      options: [], // Initialement vide
    },
    { type: 'text', name: 'title', placeholder: 'Titre de l\'article', required: true },
    { type: 'textarea', name: 'content', placeholder: 'Contenu de l\'article', required: true }
  ]);

  readonly topics = toSignal(
    this.topicDataSource.getAll().pipe(
      takeUntilDestroyed()
    ),
    { initialValue: [] }
  );

  readonly topicOptions = computed(() =>
    this.topics().map(t => ({
      value: t.id.toString(),
      label: t.name
    }))
  );

  onFormSubmit(values: Partial<Article>): void {
    this.message.set('Mise à jour en cours...');
    this.startSubmit();

    values.createdAt = new Date().toISOString();
    values.updatedAt = new Date().toISOString();

    this.articleDataSource.create(values as Article).pipe(
      //takeUntilDestroyed(),
      catchError((error) => {
        this.message.set('Erreur lors de la création de l\'article :  <br>' + error.message);
        this.error.set(true);
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    ).subscribe({
      next: () => {
        this.message.set('Création de l\'article réussie');
        this.router.navigate(['/articles']);
      }
    });
  }
}
