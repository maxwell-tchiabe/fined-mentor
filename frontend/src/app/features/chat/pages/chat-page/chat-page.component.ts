import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, combineLatest, switchMap, tap, catchError, of } from 'rxjs';
import { ChatPanelComponent } from '../../components/chat-panel/chat-panel.component';
import { DashboardPanelComponent } from '../../../dashboard/components/dashboard-panel/dashboard-panel.component';
import { QuizPanelComponent } from '../../../quiz/components/quiz-panel/quiz-panel.component';
import { NavPanelComponent } from '../../../../shared/components/nav-panel/nav-panel.component';
import { ChatSessionService } from '../../../../core/services/chat-session.service';
import { ChatMessageService } from '../../../../core/services/chat-message.service';
import { QuizService } from '../../../../core/services/quiz.service';
import { ChatMessage, ChatSession, Quiz, QuizState } from '../../../../core/models/chat.model';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, ChatPanelComponent, QuizPanelComponent, NavPanelComponent, DashboardPanelComponent],
  templateUrl: './chat-page.component.html',
  styleUrls: ['./chat-page.component.css']
})
export class ChatPageComponent implements OnInit, OnDestroy {
  private subscriptions = new Subscription();

  // Signals for state management
  isNavOpen = signal(true);
  currentView = signal<'chat' | 'dashboard'>('chat');
  isChatLoading = signal(false);
  isQuizLoading = signal(false);

  // Signals converted from observables
  sessions = toSignal(this.chatSessionService.sessions$, { initialValue: [] });
  activeSession = toSignal(this.chatSessionService.activeSession$, { initialValue: null });

  constructor(
    private chatSessionService: ChatSessionService,
    private chatMessageService: ChatMessageService,
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.route.params.subscribe(params => {
        const sessionId = params['sessionId'];
        if (sessionId) {
          this.chatSessionService.getSession(sessionId).subscribe();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onSendMessage(message: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    this.isChatLoading.set(true);

    // Check if message is a quiz request
    const testMatch = message.match(/test me on\s+(.+)/i);
    if (testMatch && testMatch[1]) {
      const topic = testMatch[1].trim();
      this.generateQuiz(topic);
      return;
    }

    // Regular chat message
    this.chatMessageService.sendMessage(activeSession.id, message).subscribe({
      next: (response) => {
        this.isChatLoading.set(false);
        // The session will be updated via the service observable
      },
      error: (error) => {
        console.error('Failed to send message:', error);
        this.isChatLoading.set(false);
      }
    });
  }

  generateQuiz(topic: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    this.isQuizLoading.set(true);

    this.quizService.generateQuiz(topic, activeSession.id).subscribe({
      next: (quiz) => {
        this.isQuizLoading.set(false);
        // Start the quiz automatically
        this.quizService.startQuiz(quiz.id, activeSession.id).subscribe();
      },
      error: (error) => {
        console.error('Failed to generate quiz:', error);
        this.isQuizLoading.set(false);
      }
    });
  }

  onAnswerSubmit(answer: string): void {
    const activeSession = this.activeSession();
    if (!activeSession?.quizState?.id) return;

    this.quizService.submitAnswer(activeSession.quizState.id, activeSession.quizState.currentQuestionIndex, answer)
      .subscribe();
  }

  onNextQuestion(): void {
    const activeSession = this.activeSession();
    const quizState = activeSession?.quizState;
    const quiz = activeSession?.quiz;
    if (!quizState || !quiz) return;

    if (quizState.currentQuestionIndex < quiz.questions.length - 1) {
      // Update local state for next question
      const updatedSession = {
        ...activeSession,
        quizState: {
          ...quizState,
          currentQuestionIndex: quizState.currentQuestionIndex + 1
        }
      } as ChatSession;
      this.chatSessionService.setActiveSession(updatedSession);
    } else {
      // Finish quiz
      this.quizService.finishQuiz(quizState.id!).subscribe();
    }
  }

  onRestartQuiz(): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    // Clear quiz from session
    const updatedSession = {
      ...activeSession,
      quiz: null,
      quizState: null
    };
    this.chatSessionService.setActiveSession(updatedSession);
  }

  onNewChat(): void {
    this.chatSessionService.createSession('New Chat').subscribe({
      next: (session) => {
        this.router.navigate(['/chat', session.id]);
      }
    });
  }

  onSelectSession(sessionId: string): void {
    this.router.navigate(['/chat', sessionId]);
  }

  onDeleteSession(sessionId: string): void {
    this.chatSessionService.deactivateSession(sessionId).subscribe();
  }

  onShowDashboard(): void {
    this.currentView.set('dashboard');
    this.router.navigate(['/dashboard']);
  }

  toggleNav(): void {
    this.isNavOpen.update(open => !open);
  }
}