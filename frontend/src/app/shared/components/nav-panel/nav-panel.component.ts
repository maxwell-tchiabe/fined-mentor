import { Component, Input, Output, EventEmitter, OnDestroy, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ChatSession } from '../../../core/models/chat.model';
import { AuthService } from '../../../core/services/auth.service';
import { Subscription } from 'rxjs';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { LanguageSwitcherComponent } from '../language-switcher/language-switcher.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

interface SessionGroup {
  label: string;
  sessions: ChatSession[];
}

@Component({
  selector: 'app-nav-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent, LanguageSwitcherComponent, TranslateModule],
  templateUrl: './nav-panel.component.html',
  styleUrls: ['./nav-panel.component.css']
})
export class NavPanelComponent implements OnInit, OnChanges, OnDestroy {
  @Input() public sessions: ChatSession[] | null = [];
  @Input() public activeSessionId: string | null = null;
  @Input() public currentView: 'chat' | 'dashboard' = 'chat';
  @Output() public newChat = new EventEmitter<void>();
  @Output() public selectSession = new EventEmitter<string>();
  @Output() public deleteSession = new EventEmitter<string>();
  @Output() public showDashboard = new EventEmitter<void>();
  @Output() public updateSessionTitle = new EventEmitter<{ sessionId: string, newTitle: string }>();

  public userName: string = 'User';
  public isMenuOpen: boolean = false;
  public sessionGroups: SessionGroup[] = [];

  // Confirm Dialog State
  public isDeleteModalOpen = false;
  public sessionToDeleteId: string | null = null;
  public deleteModalTitle = 'Delete chat?';
  public deleteModalMessage = 'Once deleted, this chat cannot be restored, and any sharing links from it will be disabled. Delete?';

  // Edit Title State
  public editingSessionId: string | null = null;
  public editedTitle: string = '';

  private userSubscription: Subscription | undefined;
  private langSubscription: Subscription | undefined;

  constructor(
    private router: Router,
    private authService: AuthService,
    private translate: TranslateService
  ) {
    this.userSubscription = this.authService.currentUser$.subscribe(user => {
      this.userName = user?.username || 'User';
    });
  }

  ngOnInit(): void {
    this.updateGroups();
    // Re-group when language changes to update month names
    this.langSubscription = this.translate.onLangChange.subscribe(() => {
      this.updateGroups();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sessions']) {
      this.updateGroups();
    }
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
    this.langSubscription?.unsubscribe();
  }

  private updateGroups(): void {
    const rawSessions = this.sessions || [];
    if (rawSessions.length === 0) {
      this.sessionGroups = [];
      return;
    }

    const sorted = [...rawSessions].sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    const groups: SessionGroup[] = [];
    const locale = this.translate.currentLang || localStorage.getItem('language') || 'en';

    sorted.forEach(session => {
      const date = new Date(session.createdAt);
      const monthYear = new Intl.DateTimeFormat(locale, { month: 'long', year: 'numeric' }).format(date);

      let group = groups.find(g => g.label === monthYear);
      if (!group) {
        group = { label: monthYear, sessions: [] };
        groups.push(group);
      }
      group.sessions.push(session);
    });

    this.sessionGroups = groups;
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

  public startEditTitle(event: Event, sessionId: string, currentTitle: string): void {
    event.stopPropagation();
    this.editingSessionId = sessionId;
    this.editedTitle = currentTitle;
  }

  public saveTitle(event: Event, sessionId: string): void {
    event.stopPropagation();
    const trimmedTitle = this.editedTitle.trim();

    if (trimmedTitle && trimmedTitle !== this.sessions?.find(s => s.id === sessionId)?.title) {
      this.updateSessionTitle.emit({ sessionId, newTitle: trimmedTitle });
    }

    this.cancelEdit();
  }

  public cancelEdit(): void {
    this.editingSessionId = null;
    this.editedTitle = '';
  }

  public isEditing(sessionId: string): boolean {
    return this.editingSessionId === sessionId;
  }
}