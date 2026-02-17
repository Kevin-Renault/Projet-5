import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ARTICLE_DATASOURCE } from 'src/app/core/services/article-datasource.interface';
import { USER_DATASOURCE } from 'src/app/core/services/user-datasource.interface';
import { HeaderComponent } from "src/app/shared/header/header.component";
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonComponent } from 'src/app/shared/common-component';


@Component({
  selector: 'app-article-list',
  imports: [FormsModule, DatePipe, SlicePipe, HeaderComponent],
  templateUrl: './article-list.component.html',
  styleUrls: ['./article-list.component.scss']
})
export class ArticleListComponent extends CommonComponent {

  private readonly userDataSource = inject(USER_DATASOURCE);
  private readonly articleDataSource = inject(ARTICLE_DATASOURCE);
  private readonly router = inject(Router);

  sortOrder = signal<'asc' | 'desc'>('desc');

  readonly articles = toSignal(
    this.articleDataSource.getAll(),
    { initialValue: [] }
  );

  readonly articlesSorted = computed(() => {
    const articles = this.articles();
    return articles.sort((a, b) => {
      const dateA = new Date(a.createdAt).getTime();
      const dateB = new Date(b.createdAt).getTime();
      return this.sortOrder() === 'asc' ? dateA - dateB : dateB - dateA;
    });
  });

  readonly users = toSignal(
    this.userDataSource.getAll(),
    { initialValue: [] }
  );

  readonly userNames = computed(() => {
    return this.users().reduce((acc, user) => {
      acc[user.id] = user.username; // Clé = id, Valeur = username
      return acc;
    }, {} as Record<number, string>); // Typage explicite
  });

  protected override computeIsPageLoading(): boolean {
    // Logique personnalisée pour la classe enfant
    return this.articles().length == 0 || this.users().length == 0; // Exemple : ne vérifie que article$
  }

  public authorById(id: number): string {
    return this.userNames()[id] || 'Inconnu';
  }

  public createArticle() {
    this.router.navigate(['/articles/create']);
  }

  public toggleSortOrder() {
    this.sortOrder.set(this.sortOrder() === 'asc' ? 'desc' : 'asc');
  }

  public openArticleComments(articleId: number) {
    this.router.navigate(['/articles', articleId, 'comment']);
  }
}
