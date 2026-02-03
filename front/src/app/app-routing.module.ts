import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
  { path: 'test', loadComponent: () => import('./features/header-test.component').then(m => m.HeaderTestComponent) },
  { path: 'user/register', loadComponent: () => import('./features/user/register/register.component').then(m => m.RegisterComponent) }
];
