import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ChatSession } from '../../../core/models/chat.model';

@Component({
  selector: 'app-nav-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nav-panel.component.html',
  styleUrls: ['./nav-panel.component.css']
})
export class NavPanelComponent {
  @Input() sessions: ChatSession[] | null = [];
  @Input() activeSessionId: string | null = null;
  @Input() currentView: 'chat' | 'dashboard' = 'chat';
  @Output() newChat = new EventEmitter<void>();
  @Output() selectSession = new EventEmitter<string>();
  @Output() deleteSession = new EventEmitter<string>();
  @Output() showDashboard = new EventEmitter<void>();

  constructor(private router: Router) {}

  get sortedSessions(): ChatSession[] {
    return [...(this.sessions || [])].sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  onNewChat(): void {
    this.newChat.emit();
  }

  onSelectSession(sessionId: string): void {
    this.selectSession.emit(sessionId);
    this.router.navigate(['/chat', sessionId]);
  }

  onDeleteSession(event: Event, sessionId: string): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this chat?')) {
      this.deleteSession.emit(sessionId);
    }
  }

  onShowDashboard(): void {
    this.showDashboard.emit();
    this.router.navigate(['/dashboard']);
  }

  isSessionActive(sessionId: string): boolean {
    return this.activeSessionId === sessionId && this.currentView === 'chat';
  }
}