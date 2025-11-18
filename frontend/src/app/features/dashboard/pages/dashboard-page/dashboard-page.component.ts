import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
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
export class DashboardPageComponent implements OnInit, OnDestroy {
  private subscriptions = new Subscription();
  
  isNavOpen = signal(true);
  sessions = this.chatSessionService.sessions$;

  constructor(private chatSessionService: ChatSessionService) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.chatSessionService.sessions$.subscribe()
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  toggleNav(): void {
    this.isNavOpen.update(open => !open);
  }

  onNewChat(): void {
    // Navigate to chat page - implementation depends on your routing
  }

  onSelectSession(sessionId: string): void {
    // Navigate to chat page - implementation depends on your routing
  }

  onDeleteSession(sessionId: string): void {
    this.chatSessionService.deactivateSession(sessionId).subscribe();
  }

  onShowDashboard(): void {
    // Already on dashboard
  }
}