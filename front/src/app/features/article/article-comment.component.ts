import { DatePipe, SlicePipe } from '@angular/common';
import { Component } from '@angular/core';
import { Article } from 'src/app/core/models/article.model';

@Component({
  selector: 'app-comment',
  imports: [DatePipe, SlicePipe],
  templateUrl: './article-comment.component.html',
  styleUrls: ['./article-comment.component.scss']
})
export class ArticleCommentComponent {
  article: Article = {
    title: 'Sample Article',
    content: 'This is a sample article content.',
    id: 0,
    createdAt: new Date().toDateString(),
    authorId: 1,
    topicId: 2
  };

  comments = [
    {
      author: 'Author 1',
      content: 'Content of comment 1...'
    },
    {
      author: 'Author 2',
      content: 'Content of comment 2...'
    },
    {
      author: 'Author 3',
      content: 'Content of comment 3...'
    },
    {
      author: 'Author 4',
      content: 'Content of comment 4...'
    }
  ];

  public authorNamebyId(id: number): string {
    return 'Author Name';
  }
  public topicNamebyId(id: number): string {
    return 'Topic Name';
  }
}
