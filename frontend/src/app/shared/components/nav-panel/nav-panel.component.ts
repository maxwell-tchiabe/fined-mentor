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
  @Input() public sessions: ChatSession[] | null = [];
  @Input() public activeSessionId: string | null = null;
  @Input() public currentView: 'chat' | 'dashboard' = 'chat';
  @Output() public newChat = new EventEmitter<void>();
  @Output() public selectSession = new EventEmitter<string>();
  @Output() public deleteSession = new EventEmitter<string>();
  @Output() public showDashboard = new EventEmitter<void>();

  constructor(private router: Router) { }

  public get sortedSessions(): ChatSession[] {
    return [...(this.sessions || [])].sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  public onNewChat(): void {
    this.newChat.emit();
  }

  public onSelectSession(sessionId: string): void {
    this.selectSession.emit(sessionId);
    this.router.navigate(['/chat', sessionId]);
  }

  public onDeleteSession(event: Event, sessionId: string): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this chat?')) {
      this.deleteSession.emit(sessionId);
    }
  }

  public onShowDashboard(): void {
    this.showDashboard.emit();
    this.router.navigate(['/dashboard']);
  }

  public isSessionActive(sessionId: string): boolean {
    return this.activeSessionId === sessionId && this.currentView === 'chat';
  }
}