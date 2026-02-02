import { Observable } from 'rxjs';
import { User } from '../models/user.model';

export interface UserDataSource {
    getAll(): Observable<User[]>;
    getById(id: number): Observable<User>;
    create(user: User): Observable<User>;
    update(id: number, user: Partial<User>): Observable<User>;
    delete(id: number): Observable<void>;
}
