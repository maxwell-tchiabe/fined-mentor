import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, switchMap, tap, catchError, of, EMPTY } from 'rxjs';
import { ChatPanelComponent } from '../../components/chat-panel/chat-panel.component';
import { DashboardPanelComponent } from '../../../dashboard/components/dashboard-panel/dashboard-panel.component';
import { QuizPanelComponent } from '../../../quiz/components/quiz-panel/quiz-panel.component';
import { NavPanelComponent } from '../../../../shared/components/nav-panel/nav-panel.component';
import { ChatSessionService } from '../../../../core/services/chat-session.service';
import { ChatMessageService } from '../../../../core/services/chat-message.service';
import { QuizService } from '../../../../core/services/quiz.service';
import { StreamingService } from '../../../../core/services/streaming.service';
import { ChatMessage, ChatSession, QuizState } from '../../../../core/models/chat.model';
import { TranslateModule } from '@ngx-translate/core';

import { MessageService } from 'primeng/api';

import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, ChatPanelComponent, QuizPanelComponent, NavPanelComponent, DashboardPanelComponent, TranslateModule],
  templateUrl: './chat-page.component.html',
  styleUrls: ['./chat-page.component.css']
})
export class ChatPageComponent implements OnInit, OnDestroy {
  public isNavOpen = signal(true);
  public currentView = signal<'chat' | 'dashboard'>('chat');
  public mobileTab = signal<'chat' | 'quiz'>('chat');
  public isChatLoading = signal(false);
  public isQuizLoading = signal(false);

  public sessions = toSignal(this.chatSessionService.sessions$, { initialValue: [] });
  public activeSession = toSignal(this.chatSessionService.activeSession$, { initialValue: null });
  // errorMessage signal removed in favor of MessageService

  private sessionSubscription: Subscription | undefined;

  constructor(
    private chatSessionService: ChatSessionService,
    private chatMessageService: ChatMessageService,
    private quizService: QuizService,
    private streamingService: StreamingService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private translate: TranslateService
  ) { }

  public ngOnInit(): void {
    this.sessionSubscription = this.route.params.pipe(
      switchMap(params => {
        const sessionId = params['sessionId'];
        if (sessionId) {
          console.log('ChatPage: Fetching session', sessionId);
          return this.chatSessionService.getSession(sessionId).pipe(
            tap(session => {
              if (session.quizState) {
                console.log('ChatPage: Loaded session with quiz state test:', session.quizState.currentQuestionIndex);
              }
            }),
            catchError(err => {
              console.error('Failed to get session:', err);
              this.router.navigate(['/chat']);
              return EMPTY;
            })
          );
        }
        return of(null);
      })
    ).subscribe(session => {
      if (session) {
        this.chatSessionService.setActiveSession(session);
      }
    });
  }

  public ngOnDestroy(): void {
    this.sessionSubscription?.unsubscribe();
  }

  public setErrorMessage(messageKey: string | null = null, fallbackMessage: string = ''): void {
    // If a key is provided (starts with 'TOAST.'), translate it. Otherwise use the message as is (if it's a backend error)
    // For consistency, we'll try to use keys where possible.
    let detail = fallbackMessage;
    if (messageKey && messageKey.startsWith('TOAST.')) {
      detail = this.translate.instant(messageKey);
    } else if (messageKey) {
      detail = messageKey;
    }

    this.messageService.add({
      severity: 'error',
      summary: this.translate.instant('TOAST.ERROR'),
      detail: detail,
      life: 5000
    });
  }

  public onSendMessage(message: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    const tempUserMessage: ChatMessage = {
      id: `tmp-user-${Date.now()}`,
      role: 'USER',
      text: message,
      timestamp: new Date()
    };

    const tempAiMessage: ChatMessage = {
      id: `tmp-ai-${Date.now()}`,
      role: 'MODEL',
      text: '', // Start empty, no dots as requested
      timestamp: new Date()
    };

    const optimisticSession: ChatSession = {
      ...activeSession,
      messages: [...(activeSession.messages || []), tempUserMessage, tempAiMessage]
    };

    this.chatSessionService.setActiveSession(optimisticSession);
    this.isChatLoading.set(true);

    let accumulatedText = '';

    this.streamingService.getStream('/chat/stream', { message, chatSessionId: activeSession.id }).pipe(
      tap(char => {
        accumulatedText += char;
        const current = this.activeSession();
        if (!current) return;

        const updatedMessages = [...(current.messages || [])];
        const lastMessageIdx = updatedMessages.length - 1;

        if (lastMessageIdx >= 0 && updatedMessages[lastMessageIdx].role === 'MODEL') {
          updatedMessages[lastMessageIdx] = {
            ...updatedMessages[lastMessageIdx],
            text: accumulatedText
          };
        }

        const updatedSession = { ...current, messages: updatedMessages };
        this.chatSessionService.setActiveSession(updatedSession);

        // Hide global loading as soon as we start seeing the stream
        if (this.isChatLoading()) {
          this.isChatLoading.set(false);
        }
      }),
      catchError(error => {
        console.error('Streaming failed:', error);
        this.setErrorMessage('TOAST.RESPONSE_FAILED');
        this.isChatLoading.set(false);
        return EMPTY;
      })
    ).subscribe({
      complete: () => {
        this.isChatLoading.set(false);
        // Refresh session to get actual IDs and persistence
        this.chatSessionService.getSession(activeSession.id).subscribe(finalSession => {
          this.chatSessionService.setActiveSession(finalSession);
        });
      }
    });
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
          this.mobileTab.set('quiz');
        })
      )),
      catchError((error: any) => {
        console.error('Failed to generate or start quiz:', error);
        this.setErrorMessage('TOAST.QUIZ_GEN_FAILED');
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

    // Persist to backend
    if (quizState.id) {
      this.quizService.updateCurrentQuestionIndex(quizState.id, newIndex).subscribe({
        error: (err) => console.error('Failed to update question index:', err)
      });
    }
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
        if (!current || !current.quizState) return;

        // Defensive merge to prevent state loss, explicitly preserving the isFinished flag
        const preservedIsFinished = finishedQuizState.finished;
        console.log('Preserved isFinished:', preservedIsFinished);
        const newQuizState: QuizState = {
          ...current.quizState,
          ...finishedQuizState,
          finished: preservedIsFinished
        };

        const updatedSession = { ...current, quizState: newQuizState };
        console.log('Setting new active session:', updatedSession);
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
      tap(() => {
        this.messageService.add({
          severity: 'success',
          summary: this.translate.instant('TOAST.SUCCESS'),
          detail: this.translate.instant('TOAST.SESSION_DELETED')
        });
      }),
      catchError(err => {
        console.error('Failed to delete session:', err);
        this.setErrorMessage('TOAST.SESSION_DELETE_FAILED');
        return of(null);
      })
    ).subscribe();
  }

  public onUpdateSessionTitle(event: { sessionId: string, newTitle: string }): void {
    this.chatSessionService.updateSessionTitle(event.sessionId, event.newTitle).pipe(
      tap(() => {
        this.messageService.add({
          severity: 'success',
          summary: this.translate.instant('TOAST.SUCCESS'),
          detail: this.translate.instant('TOAST.TITLE_UPDATED')
        });
      }),
      catchError(err => {
        console.error('Failed to update session title:', err);
        this.setErrorMessage('TOAST.TITLE_UPDATE_FAILED');
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