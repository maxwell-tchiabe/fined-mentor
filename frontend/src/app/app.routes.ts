import { Routes } from '@angular/router';
import { SessionGuard } from './core/guards/session.guard';

export const routes: Routes = [
  { 
    path: '', 
    redirectTo: '/chat', 
    pathMatch: 'full' 
  },
  {
    path: 'chat',
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
    loadComponent: () => import('./features/dashboard/pages/dashboard-page/dashboard-page.component')
      .then(c => c.DashboardPageComponent)
  },
  { 
    path: '**', 
    redirectTo: '/chat' 
  }
];