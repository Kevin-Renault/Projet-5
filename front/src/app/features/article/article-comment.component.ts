import { AsyncPipe, DatePipe, SlicePipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { map, Observable, shareReplay } from 'rxjs';
import { Article } from 'src/app/core/models/article.model';
import { TOPIC_DATASOURCE, TopicDataSource } from 'src/app/core/services/topic-datasource.interface';
import { USER_DATASOURCE, UserDataSource } from 'src/app/core/services/user-datasource.interface';
import { ARTICLE_DATASOURCE, ArticleDataSource } from 'src/app/core/services/article-datasource.interface';
import { COMMENT_DATASOURCE, ArticleCommentDataSource } from 'src/app/core/services/article-comment-datasource.interface';
import { ActivatedRoute } from '@angular/router';
import { ArticleComment } from 'src/app/core/models/article-comment.model';

@Component({
  selector: 'app-comment',
  imports: [DatePipe, SlicePipe, AsyncPipe],
  templateUrl: './article-comment.component.html',
  styleUrls: ['./article-comment.component.scss']
})
export class ArticleCommentComponent {


  private readonly authorCache = new Map<number, Observable<string>>();
  private readonly topicCache = new Map<number, Observable<string>>();
  readonly articleID: number | null = null;
  article$: Observable<Article> | null = null;
  articleComments$: Observable<ArticleComment[]> | null = null;


  constructor(
    @Inject(USER_DATASOURCE) private readonly userDataSource: UserDataSource,
    @Inject(TOPIC_DATASOURCE) private readonly topicDataSource: TopicDataSource,
    @Inject(ARTICLE_DATASOURCE) private readonly articleDataSource: ArticleDataSource,
    @Inject(COMMENT_DATASOURCE) private readonly commentDataSource: ArticleCommentDataSource,
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
}
