import { AsyncPipe, DatePipe } from '@angular/common';
import { Component, Inject, OnDestroy } from '@angular/core';
import { map, Observable, shareReplay, Subject, takeUntil } from 'rxjs';
import { Article } from 'src/app/core/models/article.model';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { USER_DATASOURCE, UserDataSource } from 'src/app/core/services/user-datasource.interface';
import { ARTICLE_DATASOURCE, ArticleDataSource } from 'src/app/core/services/article-datasource.interface';
import { COMMENT_DATASOURCE, ArticleCommentDataSource } from 'src/app/core/services/article-comment-datasource.interface';
import { ActivatedRoute } from '@angular/router';
import { ArticleComment } from 'src/app/core/models/article-comment.model';
import { DynamicFormComponent, FormElement } from "src/app/shared/form/dynamic-form.component";
import { MatSnackBar } from '@angular/material/snack-bar';
import { AUTH_DATASOURCE, AuthDataSource } from 'src/app/core/auth/auth-datasource.interface';

@Component({
  selector: 'app-comment',
  imports: [DatePipe, AsyncPipe, DynamicFormComponent],
  templateUrl: './article-comment.component.html',
  styleUrls: ['./article-comment.component.scss']
})
export class ArticleCommentComponent implements OnDestroy {

  commentFormElements: FormElement[] = [
    { type: 'textarea', name: 'content', placeholder: 'Contenu du commentaire', required: true }
  ];



  private readonly authorCache = new Map<number, Observable<string>>();
  private readonly topicCache = new Map<number, Observable<string>>();
  readonly articleID: number | null = null;
  article$: Observable<Article> | null = null;
  articleComments$: Observable<ArticleComment[]> | null = null;


  constructor(
    @Inject(AUTH_DATASOURCE) private readonly authDataSource: AuthDataSource,
    @Inject(USER_DATASOURCE) private readonly userDataSource: UserDataSource,
    @Inject(TOPIC_DATASOURCE) private readonly topicDataSource: TopicDataSource,
    @Inject(ARTICLE_DATASOURCE) private readonly articleDataSource: ArticleDataSource,
    @Inject(COMMENT_DATASOURCE) private readonly commentDataSource: ArticleCommentDataSource,
    private readonly snackBar: MatSnackBar,
    private readonly route: ActivatedRoute
  ) {
    this.articleID = Number(this.route.snapshot.paramMap.get('id'));
    if (this.articleID) {
      this.article$ = this.articleDataSource.getById(this.articleID);
      this.articleComments$ = this.commentDataSource.getAllByArticleId(this.articleID);
    }
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

    values.createdAt = new Date().toISOString();
    const authorId = this.authDataSource.getCurrentUserId();
    console.log('Current user ID:', authorId);
    if (typeof authorId === 'number') {
      values.authorId = authorId;
      values.articleId = this.articleID!;
      this.commentDataSource.create(values as ArticleComment).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: () => {
          this.articleComments$ = this.commentDataSource.getAllByArticleId(this.articleID!);
          // Réinitialise le formulaire dynamique
          const form = document.querySelector('form.dynamic-form') as HTMLFormElement;
          if (form) form.reset();
        },
        error: () => alert('Erreur lors de la création de l\'article'),
      });
    } else {
      alert('No authenticated user. Cannot post comment.');
    }
  }
}
