import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, switchMap, tap, catchError, of, EMPTY } from 'rxjs';
import { ChatPanelComponent } from '../../components/chat-panel/chat-panel.component';
import { DashboardPanelComponent } from '../../../dashboard/components/dashboard-panel/dashboard-panel.component';
import { QuizPanelComponent } from '../../../quiz/components/quiz-panel/quiz-panel.component';
import { NavPanelComponent } from '../../../../shared/components/nav-panel/nav-panel.component';
import { ChatSessionService } from '../../../../core/services/chat-session.service';
import { ChatMessageService } from '../../../../core/services/chat-message.service';
import { QuizService } from '../../../../core/services/quiz.service';
import { ChatMessage, ChatSession, QuizState } from '../../../../core/models/chat.model';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, ChatPanelComponent, QuizPanelComponent, NavPanelComponent, DashboardPanelComponent],
  templateUrl: './chat-page.component.html',
  styleUrls: ['./chat-page.component.css']
})
export class ChatPageComponent implements OnInit, OnDestroy {
  public isNavOpen = signal(true);
  public currentView = signal<'chat' | 'dashboard'>('chat');
  public isChatLoading = signal(false);
  public isQuizLoading = signal(false);

  public sessions = toSignal(this.chatSessionService.sessions$, { initialValue: [] });
  public activeSession = toSignal(this.chatSessionService.activeSession$, { initialValue: null });

  private sessionSubscription: Subscription | undefined;

  constructor(
    private chatSessionService: ChatSessionService,
    private chatMessageService: ChatMessageService,
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  public ngOnInit(): void {
    this.sessionSubscription = this.route.params.pipe(
      switchMap(params => {
        const sessionId = params['sessionId'];
        if (sessionId) {
          return this.chatSessionService.getSession(sessionId).pipe(
            catchError(err => {
              console.error('Failed to get session:', err);
              this.router.navigate(['/chat']); // Redirect on error
              return EMPTY;
            })
          );
        }
        return of(null);
      })
    ).subscribe();
  }

  public ngOnDestroy(): void {
    this.sessionSubscription?.unsubscribe();
  }

  public onSendMessage(message: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    const tempMessage: ChatMessage = {
      id: `tmp-${Date.now()}`,
      role: 'user',
      text: message,
      timestamp: new Date()
    };

    const optimisticSession: ChatSession = {
      ...activeSession,
      messages: [...(activeSession.messages || []), tempMessage]
    };

    this.chatSessionService.setActiveSession(optimisticSession);
    this.isChatLoading.set(true);

    const testMatch = message.match(/test me on\s+(.+)/i);
    if (testMatch && testMatch[1]) {
      const topic = testMatch[1].trim();
      this.generateQuiz(topic);
      this.isChatLoading.set(false);
      return;
    }

    this.chatMessageService.sendMessage(activeSession.id, message).pipe(
      tap(response => {
        const current = this.activeSession();
        if (!current) return;

        let updatedMessages = [...(current.messages || [])];
        const tmpIndex = updatedMessages.findIndex(m => m.id && m.id.toString().startsWith('tmp-'));

        if (tmpIndex >= 0 && response.id) {
          updatedMessages[tmpIndex] = response;
        } else {
          updatedMessages.push(response);
        }

        const updatedSession = { ...current, messages: updatedMessages };
        this.chatSessionService.setActiveSession(updatedSession);
      }),
      catchError(error => {
        console.error('Failed to send message:', error);
        return of(null); // Keep the optimistic message
      })
    ).subscribe(() => this.isChatLoading.set(false));
  }

  public generateQuiz(topic: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    this.isQuizLoading.set(true);

    this.quizService.generateQuiz(topic, activeSession.id).pipe(
      switchMap(quiz => this.quizService.startQuiz(quiz.id, activeSession.id).pipe(
        tap(quizState => {
          const current = this.activeSession();
          if (!current) return;
          const updatedSession = { ...current, quiz, quizState };
          this.chatSessionService.setActiveSession(updatedSession);
        })
      )),
      catchError(error => {
        console.error('Failed to generate or start quiz:', error);
        return of(null);
      })
    ).subscribe(() => this.isQuizLoading.set(false));
  }

  public onAnswerSubmit(answer: string): void {
    const activeSession = this.activeSession();
    if (!activeSession?.quizState?.id) return;

    console.log(`Submitting answer for question index: ${activeSession.quizState.currentQuestionIndex}`);

    this.quizService.submitAnswer(activeSession.quizState.id, activeSession.quizState.currentQuestionIndex, answer).pipe(
      tap(updatedQuizState => {
        console.log('Received updated quiz state from backend:', updatedQuizState);
        const current = this.activeSession();
        if (!current || !current.quizState) return;

        // Defensive merge to prevent state loss, explicitly preserving the current index
        const preservedIndex = current.quizState.currentQuestionIndex;
        const newQuizState: QuizState = {
          ...current.quizState,
          ...updatedQuizState,
          currentQuestionIndex: preservedIndex
        };

        const updatedSession = { ...current, quizState: newQuizState };
        console.log('Setting new active session:', updatedSession);
        this.chatSessionService.setActiveSession(updatedSession);
      }),
      catchError(err => {
        console.error('Failed to submit answer:', err);
        return of(null);
      })
    ).subscribe();
  }

  public onNextQuestion(): void {
    const activeSession = this.activeSession();
    const quizState = activeSession?.quizState;
    if (!activeSession || !quizState) return;

    const newIndex = quizState.currentQuestionIndex + 1;
    console.log(`Moving to next question index: ${newIndex}`);

    const updatedSession: ChatSession = {
      ...activeSession,
      quizState: {
        ...quizState,
        currentQuestionIndex: newIndex
      }
    };
    this.chatSessionService.setActiveSession(updatedSession);
  }

  public onPreviousQuestion(): void {
    const activeSession = this.activeSession();
    const quizState = activeSession?.quizState;
    if (!activeSession || !quizState) return;

    const newIndex = quizState.currentQuestionIndex - 1;
    console.log(`Moving to previous question index: ${newIndex}`);

    const updatedSession: ChatSession = {
      ...activeSession,
      quizState: {
        ...quizState,
        currentQuestionIndex: newIndex
      }
    };
    this.chatSessionService.setActiveSession(updatedSession);
  }

  public onFinishQuiz(): void {
    const activeSession = this.activeSession();
    const quizState = activeSession?.quizState;
    if (!quizState?.id) return;

    console.log('Finishing quiz...');

    this.quizService.finishQuiz(quizState.id).pipe(
      tap(finishedQuizState => {
        console.log('Received finished quiz state:', finishedQuizState);
        const current = this.activeSession();
        if (!current) return;
        const updatedSession = { ...current, quizState: finishedQuizState };
        this.chatSessionService.setActiveSession(updatedSession);
      }),
      catchError(err => {
        console.error('Failed to finish quiz:', err);
        return of(null);
      })
    ).subscribe();
  }

  public onRestartQuiz(): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    const updatedSession: any = {
      ...activeSession,
      quiz: null,
      quizState: null
    };
    this.chatSessionService.setActiveSession(updatedSession);
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

  public onShowDashboard(): void {
    this.currentView.set('dashboard');
    this.router.navigate(['/dashboard']);
  }

  public toggleNav(): void {
    this.isNavOpen.update(open => !open);
  }
}