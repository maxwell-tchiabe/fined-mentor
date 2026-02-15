import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { Subscription, combineLatest } from 'rxjs';
import { AuthService } from './core/services/auth.service';
import { ChatSessionService } from './core/services/chat-session.service';
import { ChatMessageService } from './core/services/chat-message.service';
import { QuizService } from './core/services/quiz.service';
import { ChatSession, ChatMessage } from './core/models/chat.model';
import { TranslateService } from '@ngx-translate/core';
import { LoggerService } from './core/services/logger.service';

import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, ToastModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  private subscriptions = new Subscription();

  constructor(
    private chatSessionService: ChatSessionService,
    private chatMessageService: ChatMessageService,
    private quizService: QuizService,
    private router: Router,
    private authService: AuthService,
    private translate: TranslateService,
    private logger: LoggerService
  ) {
    this.translate.setDefaultLang('en');
    this.translate.use('en');
  }

  ngOnInit(): void {
    this.initializeApp();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private initializeApp(): void {
    // Check authentication from cookie on startup
    this.authService.checkAuth().subscribe({
      next: (response) => {
        if (response.success) {
          this.logger.log('Session restored successfully');
          this.chatSessionService.loadSessions().subscribe({
            next: () => this.logger.log('App initialized with sessions loaded'),
            error: (err) => this.logger.error('Failed to load sessions during app initialization:', err)
          });
        } else {
          this.logger.log('No active session found');
        }
      },
      error: () => {
        this.logger.log('Session check completed (no session or error)');
      }
    });
  }
}