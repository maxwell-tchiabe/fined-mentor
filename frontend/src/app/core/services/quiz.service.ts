import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { Quiz, QuizState, ApiResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class QuizService extends ApiService {

  constructor(http: HttpClient) {
    super(http);
  }

  generateQuiz(topic: string, chatSessionId: string): Observable<Quiz> {
    return this.http.post<ApiResponse<Quiz>>(
      `${this.apiUrl}/quiz/generate`,
      { topic, chatSessionId }
    ).pipe(map(response => response.data));
  }

  saveStreamedQuiz(topic: string, chatSessionId: string, quizJson: string): Observable<Quiz> {
    return this.http.post<ApiResponse<Quiz>>(
      `${this.apiUrl}/quiz/save`,
      { topic, chatSessionId, quizJson }
    ).pipe(map(response => response.data));
  }

  startQuiz(quizId: string, chatSessionId: string): Observable<QuizState> {
    return this.http.post<ApiResponse<QuizState>>(
      `${this.apiUrl}/quiz/${quizId}/start?chatSessionId=${chatSessionId}`,
      {}
    ).pipe(map(response => response.data));
  }

  submitAnswer(quizStateId: string, questionIndex: number, answer: string): Observable<QuizState> {
    return this.http.post<ApiResponse<QuizState>>(
      `${this.apiUrl}/quiz/answer`,
      { quizStateId, questionIndex, answer }
    ).pipe(map(response => response.data));
  }

  getQuizBySession(sessionId: string): Observable<Quiz> {
    return this.http.get<ApiResponse<Quiz>>(
      `${this.apiUrl}/quiz/sessions/${sessionId}`
    ).pipe(map(response => response.data));
  }

  getQuizState(quizStateId: string): Observable<QuizState> {
    return this.http.get<ApiResponse<QuizState>>(
      `${this.apiUrl}/quiz/state/${quizStateId}`
    ).pipe(map(response => response.data));
  }

  finishQuiz(quizStateId: string): Observable<QuizState> {
    return this.http.post<ApiResponse<QuizState>>(
      `${this.apiUrl}/quiz/${quizStateId}/finish`,
      {}
    ).pipe(map(response => response.data));
  }

  updateCurrentQuestionIndex(quizStateId: string, index: number): Observable<QuizState> {
    return this.http.put<ApiResponse<QuizState>>(
      `${this.apiUrl}/quiz/state/${quizStateId}/index`,
      { index }
    ).pipe(map(response => response.data));
  }
}