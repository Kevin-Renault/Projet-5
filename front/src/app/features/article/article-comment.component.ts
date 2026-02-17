import { AsyncPipe, DatePipe } from '@angular/common';
import { Component, inject, OnDestroy, signal } from '@angular/core';
import { catchError, filter, finalize, map, Observable, shareReplay, startWith, Subject, switchMap, throwError } from 'rxjs';
import { Article } from 'src/app/core/models/article.model';
import { TOPIC_DATASOURCE } from 'src/app/core/services/topic-datasource.interface';
import { USER_DATASOURCE } from 'src/app/core/services/user-datasource.interface';
import { ARTICLE_DATASOURCE } from 'src/app/core/services/article-datasource.interface';
import { COMMENT_DATASOURCE } from 'src/app/core/services/article-comment-datasource.interface';
import { ActivatedRoute } from '@angular/router';
import { ArticleComment } from 'src/app/core/models/article-comment.model';
import { DynamicFormComponent, FormElement } from "src/app/shared/form/dynamic-form.component";
import { HeaderComponent } from "src/app/shared/header/header.component";
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { CommonComponent } from 'src/app/shared/common-component';

@Component({
  selector: 'app-comment',
  imports: [DatePipe, AsyncPipe, DynamicFormComponent, HeaderComponent],
  templateUrl: './article-comment.component.html',
  styleUrls: ['./article-comment.component.scss']
})
export class ArticleCommentComponent extends CommonComponent implements OnDestroy {

  private readonly userDataSource = inject(USER_DATASOURCE);
  private readonly topicDataSource = inject(TOPIC_DATASOURCE);
  private readonly articleDataSource = inject(ARTICLE_DATASOURCE);
  private readonly commentDataSource = inject(COMMENT_DATASOURCE);
  private readonly route = inject(ActivatedRoute);


  commentFormElements: FormElement[] = [
    { type: 'textarea', name: 'content', placeholder: 'Contenu du commentaire', required: true }
  ];

  readonly articleID = signal<number | null>(null);
  private readonly refresh$ = new Subject<void>();
  private readonly authorCache = new Map<number, Observable<string>>();
  private readonly topicCache = new Map<number, Observable<string>>();
  public articleLoading = true;
  protected override computeIsPageLoading(): boolean {
    return this.articleLoading;
  }

  article$: Observable<Article> | null = null;

  private readonly articleID$ = this.route.paramMap.pipe(
    map(params => Number(params.get('id'))),
    filter(id => !Number.isNaN(id)), // Filtre les IDs invalides
    takeUntilDestroyed()
  );

  readonly articleComments = toSignal(
    this.articleID$.pipe(
      switchMap(id => {
        this.articleID.set(id);
        return this.refresh$.pipe(
          startWith(void 0),
          switchMap(() => this.commentDataSource.getAllByArticleId(id))
        );
      })
    ),
    { initialValue: [] }
  );

  refreshComments(): void {
    this.refresh$.next();
  }


  constructor() {
    super();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.articleID.set(Number.isNaN(id) ? null : id);
    console.log('Article ID from route:', this.articleID());

    this.article$ = this.articleDataSource.getById(this.articleID()!).pipe(
      takeUntilDestroyed(),
      finalize(() => {
        this.articleLoading = false;
      })
    );
  }

  public authorById(id: number): Observable<string> {
    if (!this.authorCache.has(id)) {
      this.authorCache.set(id, this.userDataSource.getById(id).pipe(
        map(user => user.username),
        shareReplay(1) // Met en cache le résultat
      ));
    }
    return this.authorCache.get(id)!;
  }

  public topicById(id: number): Observable<string> {
    if (!this.topicCache.has(id)) {
      this.topicCache.set(id, this.topicDataSource.getById(id).pipe(
        map(topic => topic.name),
        shareReplay(1)
      ));
    }
    return this.topicCache.get(id)!;
  }

  private readonly destroy$ = new Subject<void>();

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }


  onFormSubmit(values: Partial<ArticleComment>): void {
    this.message.set('Création en cours...');
    this.startSubmit();

    values.createdAt = new Date().toISOString();
    values.articleId = this.articleID() as number;

    this.commentDataSource.create(values as ArticleComment).pipe(
      //takeUntilDestroyed(),
      catchError((error) => {
        this.message.set('Erreur lors de la création du commentaire :  <br>' + error.message);
        this.error.set(true);
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    ).subscribe({
      next: () => {
        this.message.set('Création du commentaire réussie');
        this.refresh$.next();
        // Réinitialise le formulaire dynamique
        const form = document.querySelector('form.dynamic-form') as HTMLFormElement;
        if (form) form.reset();
      }
    });
  }
}
