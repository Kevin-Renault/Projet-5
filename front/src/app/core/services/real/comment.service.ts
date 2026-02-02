import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comment } from '../../models/comment.model';

@Injectable({ providedIn: 'root' })
export class CommentService {
    private readonly apiUrl = '/api/comments';

    constructor(private readonly http: HttpClient) { }

    getAll(): Observable<Comment[]> {
        return this.http.get<Comment[]>(this.apiUrl);
    }

    getById(id: number): Observable<Comment> {
        return this.http.get<Comment>(`${this.apiUrl}/${id}`);
    }

    create(comment: Comment): Observable<Comment> {
        return this.http.post<Comment>(this.apiUrl, comment);
    }

    update(id: number, comment: Partial<Comment>): Observable<Comment> {
        return this.http.put<Comment>(`${this.apiUrl}/${id}`, comment);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
