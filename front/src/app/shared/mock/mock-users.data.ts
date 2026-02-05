import { User } from '../../core/models/user.model';

export const MOCK_USERS: User[] = [
    { id: 1, username: 'alice', email: 'alice@email.com', password: 'Mock-password1', role: 'user' },
    { id: 2, username: 'bob', email: 'bob@email.com', password: 'Mock-password2', role: 'user' },
    { id: 3, username: 'charlie', email: 'charlie@email.com', password: 'Mock-password3', role: 'user' },
    { id: 4, username: 'diana', email: 'diana@email.com', password: 'Mock-password4', role: 'user' },
    { id: 5, username: 'eve', email: 'eve@email.com', password: 'Mock-password5', role: 'user' }
];
