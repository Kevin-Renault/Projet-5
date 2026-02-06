import { Component, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe, SlicePipe, AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Article } from 'src/app/core/models/article.model';
import { Observable } from 'rxjs/internal/Observable';
import { ARTICLE_DATASOURCE, ArticleDataSource } from 'src/app/core/services/article-datasource.interface';
import { map, shareReplay } from 'rxjs';
import { USER_DATASOURCE, UserDataSource } from 'src/app/core/services/user-datasource.interface';


@Component({
  selector: 'app-article-list',
  imports: [FormsModule, DatePipe, SlicePipe, AsyncPipe],
  templateUrl: './article-list.component.html',
  styleUrls: ['./article-list.component.scss']
})
export class ArticleListComponent {

  sortOrder: 'asc' | 'desc' = 'desc';
  private readonly authorCache = new Map<number, Observable<string>>();
  articles$: Observable<Article[]> | null = null;

  constructor(
    @Inject(USER_DATASOURCE) private readonly userDataSource: UserDataSource,
    @Inject(ARTICLE_DATASOURCE) private readonly articleDataSource: ArticleDataSource,
    private readonly router: Router
  ) {
    this.articles$ = this.articleDataSource.getAll().pipe(
      map(articles => this.articleDataSource.sortByDateDesc(articles)));
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

  public createArticle() {
    this.router.navigate(['/articles/create']);
  }

  public toggleSortOrder() {
    if (this.articles$) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
      this.articles$ = this.articleDataSource.getAll().pipe(
        map(articles =>
          this.sortOrder === 'asc'
            ? this.articleDataSource.sortByDateAsc(articles)
            : this.articleDataSource.sortByDateDesc(articles)
        )
      );
    }
  }

  public openArticleComments(articleId: number) {
    this.router.navigate(['/articles', articleId, 'comment']);
  }
}
