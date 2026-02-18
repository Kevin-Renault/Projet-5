import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { environment } from '../environments/environment';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
  ...(environment.enableDevRoutes
    ? [{ path: 'test', loadComponent: () => import('./features/header-test.component').then(m => m.HeaderTestComponent) }]
    : []),
  { path: 'user/register', loadComponent: () => import('./features/user/register/register.component').then(m => m.RegisterComponent) },
  { path: 'user/login', loadComponent: () => import('./features/user/login/login.component').then(m => m.LoginComponent) },
  {
    path: 'articles', loadComponent: () => import('./features/article/article-list.component').then(m => m.ArticleListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'topics', loadComponent: () => import('./features/topic/topic.component').then(m => m.TopicComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'articles/create', loadComponent: () => import('./features/article/article-create.component').then(m => m.ArticleCreateComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'articles/:id/comment', loadComponent: () => import('./features/article/article-comment.component').then(m => m.ArticleCommentComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'user/profile', loadComponent: () => import('./features/user/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [AuthGuard]
  },
  {
    path: '**', loadComponent: () => import('./shared/error/error.component').then(m => m.ErrorComponent),
    canActivate: [AuthGuard]
  }
];