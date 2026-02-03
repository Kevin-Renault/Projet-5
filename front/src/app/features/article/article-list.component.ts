import { Component } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-article-list',
  imports: [FormsModule, DatePipe, SlicePipe],
  templateUrl: './article-list.component.html',
  styleUrls: ['./article-list.component.scss']
})
export class ArticleListComponent {

  sortOrder: 'asc' | 'desc' = 'desc';

  articles = [
    {
      title: 'Article 1',
      createdAt: new Date(),
      author: 'Author 1',
      content: 'Content of article 1...'
    },
    {
      title: 'Article 2',
      createdAt: new Date(),
      author: 'Author 2',
      content: 'Content of article 2...'
    },
    {
      title: 'Article 3',
      createdAt: new Date(),
      author: 'Author 3',
      content: 'Content of article 3...'
    },
    {
      title: 'Article 4',
      createdAt: new Date(),
      author: 'Author 4',
      content: 'Content of article 4...'
    }
  ];

  public createArticle() {

  }

  public toggleSortOrder() {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
  }

}
