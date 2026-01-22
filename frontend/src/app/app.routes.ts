import { Routes } from '@angular/router';
import { SessionGuard } from './core/guards/session.guard';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing-page/landing-page.component')
      .then(c => c.LandingPageComponent)
  },
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/pages/login/login.component')
      .then(c => c.LoginComponent)
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/pages/register/register.component')
      .then(c => c.RegisterComponent)
  },
  {
    path: 'auth/activate',
    loadComponent: () => import('./features/auth/pages/activate/activate.component')
      .then(c => c.ActivateComponent)
  },
  {
    path: 'chat',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/chat/pages/chat-page/chat-page.component')
      .then(c => c.ChatPageComponent)
  },
  {
    path: 'chat/:sessionId',
    canActivate: [SessionGuard],
    loadComponent: () => import('./features/chat/pages/chat-page/chat-page.component')
      .then(c => c.ChatPageComponent)
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/dashboard/pages/dashboard-page/dashboard-page.component')
      .then(c => c.DashboardPageComponent)
  },
  {
    path: '**',
    redirectTo: '/'
  }
];