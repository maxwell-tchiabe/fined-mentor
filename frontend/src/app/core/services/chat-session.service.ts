import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, map, tap } from 'rxjs';
import { ApiService } from './api.service';
import { ChatSession, ChatMessage, ApiResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatSessionService extends ApiService {
  private sessionsSubject = new BehaviorSubject<ChatSession[]>([]);
  public sessions$ = this.sessionsSubject.asObservable();

  private activeSessionSubject = new BehaviorSubject<ChatSession | null>(null);
  public activeSession$ = this.activeSessionSubject.asObservable();

  constructor(http: HttpClient) {
    super(http);
    this.loadSessions();
  }

  createSession(title: string): Observable<ChatSession> {
    return this.http.post<ApiResponse<ChatSession>>(
      `${this.apiUrl}/chat/sessions?title=${encodeURIComponent(title)}`,
      {}
    ).pipe(
      map(response => response.data),
      tap(session => {
        const currentSessions = this.sessionsSubject.value;
        this.sessionsSubject.next([session, ...currentSessions]);
        this.activeSessionSubject.next(session);
      })
    );
  }

  getSession(sessionId: string): Observable<ChatSession> {
    return this.http.get<ApiResponse<ChatSession>>(
      `${this.apiUrl}/chat/sessions/${sessionId}`
    ).pipe(map(response => response.data));
  }

  getActiveSessions(): Observable<ChatSession[]> {
    return this.http.get<ApiResponse<ChatSession[]>>(
      `${this.apiUrl}/chat/sessions`
    ).pipe(
      map(response => response.data),
      tap(sessions => this.sessionsSubject.next(sessions))
    );
  }

  deactivateSession(sessionId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/chat/sessions/${sessionId}`
    ).pipe(
      map(response => response.data),
      tap(() => {
        const currentSessions = this.sessionsSubject.value;
        const updatedSessions = currentSessions.filter(s => s.id !== sessionId);
        this.sessionsSubject.next(updatedSessions);

        if (this.activeSessionSubject.value?.id === sessionId) {
          this.activeSessionSubject.next(updatedSessions[0] || null);
        }
      })
    );
  }

  setActiveSession(session: ChatSession): void {
    this.activeSessionSubject.next(session);

    // Also update the session in the sessions list so dashboard gets updated
    const currentSessions = this.sessionsSubject.value;
    const sessionIndex = currentSessions.findIndex(s => s.id === session.id);

    if (sessionIndex >= 0) {
      const updatedSessions = [...currentSessions];
      updatedSessions[sessionIndex] = session;
      this.sessionsSubject.next(updatedSessions);
    }
  }

  private loadSessions(): void {
    this.getActiveSessions().subscribe();
  }
}