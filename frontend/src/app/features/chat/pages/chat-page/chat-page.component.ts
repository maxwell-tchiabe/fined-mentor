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
  public errorMessage = signal<string | null>(null); 

  private sessionSubscription: Subscription | undefined;

  constructor(
    private chatSessionService: ChatSessionService,
    private chatMessageService: ChatMessageService,
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  public ngOnInit(): void {
    this.sessionSubscription = this.route.params.pipe(
      switchMap(params => {
        const sessionId = params['sessionId'];
        if (sessionId) {
          // If the session is already active, don't re-fetch
          if (this.activeSession()?.id === sessionId) {
            return of(this.activeSession());
          }
          return this.chatSessionService.getSession(sessionId).pipe(
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

  public setErrorMessage(message: string): void {
    this.errorMessage.set(message);
    setTimeout(() => this.errorMessage.set(null), 3000); 
  }

  public onSendMessage(message: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    const tempMessage: ChatMessage = {
      id: `tmp-${Date.now()}`,
      role: 'USER',
      text: message,
      timestamp: new Date()
    };

    const optimisticSession: ChatSession = {
      ...activeSession,
      messages: [...(activeSession.messages || []), tempMessage]
    };

    this.chatSessionService.setActiveSession(optimisticSession);
    this.isChatLoading.set(true);
    this.errorMessage.set(null); 

    this.chatMessageService.sendMessage(activeSession.id, message).pipe(
      tap(response => {
        const current = this.activeSession();
        if (!current) return;

        let updatedMessages = [...(current.messages || [])];

        // Append the model response. We keep the temporary user message.
        updatedMessages.push(response);

        const updatedSession = { ...current, messages: updatedMessages };
        this.chatSessionService.setActiveSession(updatedSession);
      }),
      catchError(error => {
        console.error('Failed to send message:', error);
        this.setErrorMessage('Failed to send message. Please try again.');
        return of(null);
      })
    ).subscribe(() => this.isChatLoading.set(false));
  }

  public generateQuiz(topic: string): void {
    const activeSession = this.activeSession();
    if (!activeSession) return;

    this.isQuizLoading.set(true);
    this.errorMessage.set(null); // Clear previous error

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
        this.setErrorMessage('Failed to generate quiz. Please try again.');
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