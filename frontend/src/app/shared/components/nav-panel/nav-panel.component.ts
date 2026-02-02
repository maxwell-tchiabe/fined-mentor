import { Component, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ChatSession } from '../../../core/models/chat.model';
import { AuthService } from '../../../core/services/auth.service';
import { Subscription } from 'rxjs';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-nav-panel',
  standalone: true,
  imports: [CommonModule, ConfirmDialogComponent],
  templateUrl: './nav-panel.component.html',
  styleUrls: ['./nav-panel.component.css']
})
export class NavPanelComponent implements OnDestroy {
  @Input() public sessions: ChatSession[] | null = [];
  @Input() public activeSessionId: string | null = null;
  @Input() public currentView: 'chat' | 'dashboard' = 'chat';
  @Output() public newChat = new EventEmitter<void>();
  @Output() public selectSession = new EventEmitter<string>();
  @Output() public deleteSession = new EventEmitter<string>();
  @Output() public showDashboard = new EventEmitter<void>();

  public userName: string = 'User';
  public isMenuOpen: boolean = false;

  // Confirm Dialog State
  public isDeleteModalOpen = false;
  public sessionToDeleteId: string | null = null;
  public deleteModalTitle = 'Delete chat?';
  public deleteModalMessage = 'Once deleted, this chat cannot be restored, and any sharing links from it will be disabled. Delete?';

  private userSubscription: Subscription | undefined;

  constructor(private router: Router, private authService: AuthService) {
    this.userSubscription = this.authService.currentUser$.subscribe(user => {
      this.userName = user?.username || 'User';
    });
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }

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

  public openDeleteModal(event: Event, sessionId: string): void {
    event.stopPropagation();
    this.sessionToDeleteId = sessionId;
    this.isDeleteModalOpen = true;
  }

  public onConfirmDelete(): void {
    if (this.sessionToDeleteId) {
      this.deleteSession.emit(this.sessionToDeleteId);
      this.closeDeleteModal();
    }
  }

  public onCancelDelete(): void {
    this.closeDeleteModal();
  }

  private closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
    this.sessionToDeleteId = null;
  }

  public onShowDashboard(): void {
    this.showDashboard.emit();
    this.router.navigate(['/dashboard']);
  }

  public isSessionActive(sessionId: string): boolean {
    return this.activeSessionId === sessionId && this.currentView === 'chat';
  }

  public toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  public closeMenu(): void {
    this.isMenuOpen = false;
  }

  public onLogout(): void {
    this.authService.logout();
    this.closeMenu();
  }
}