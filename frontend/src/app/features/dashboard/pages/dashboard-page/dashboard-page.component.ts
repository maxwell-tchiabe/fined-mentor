import { Component, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, of, tap } from 'rxjs';
import { DashboardPanelComponent } from '../../components/dashboard-panel/dashboard-panel.component';
import { NavPanelComponent } from '../../../../shared/components/nav-panel/nav-panel.component';
import { ChatSessionService } from '../../../../core/services/chat-session.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, DashboardPanelComponent, NavPanelComponent],
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.css']
})
export class DashboardPageComponent {
  public isNavOpen = signal(true);
  public sessions = toSignal(this.chatSessionService.sessions$, { initialValue: [] });

  constructor(
    private chatSessionService: ChatSessionService,
    private router: Router
  ) { }

  public toggleNav(): void {
    this.isNavOpen.update(open => !open);
  }

  public onNewChat(): void {
    this.chatSessionService.createSession('New Chat').pipe(
      tap(session => {
        this.router.navigate(['/chat', session.id]);
      }),
      catchError(err => {
        console.error('Failed to create new chat:', err);
        return of(null);
      })
    ).subscribe();
  }

  public onSelectSession(sessionId: string): void {
    this.router.navigate(['/chat', sessionId]);
  }

  public onDeleteSession(sessionId: string): void {
    this.chatSessionService.deactivateSession(sessionId).pipe(
      catchError(err => {
        console.error('Failed to delete session:', err);
        return of(null);
      })
    ).subscribe();
  }

  public onUpdateSessionTitle(event: { sessionId: string, newTitle: string }): void {
    this.chatSessionService.updateSessionTitle(event.sessionId, event.newTitle).pipe(
      catchError(err => {
        console.error('Failed to update session title:', err);
        return of(null);
      })
    ).subscribe();
  }

  public onShowDashboard(): void {
    // Already on dashboard
  }
}