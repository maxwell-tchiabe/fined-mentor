import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { ChatMessage, ApiResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatMessageService extends ApiService {
  
  constructor(http: HttpClient) {
    super(http);
  }

  sendMessage(chatSessionId: string, message: string): Observable<ChatMessage> {
    return this.http.post<ApiResponse<ChatMessage>>(
      `${this.apiUrl}/chat/message`,
      { message, chatSessionId }
    ).pipe(map(response => response.data));
  }

  getChatHistory(sessionId: string): Observable<ChatMessage[]> {
    return this.http.get<ApiResponse<ChatMessage[]>>(
      `${this.apiUrl}/chat/sessions/${sessionId}/history`
    ).pipe(map(response => response.data));
  }

  updateMessage(messageId: string, newText: string): Observable<ChatMessage> {
    return this.http.put<ApiResponse<ChatMessage>>(
      `${this.apiUrl}/chat/messages/${messageId}`,
      { text: newText }
    ).pipe(map(response => response.data));
  }

  deleteMessage(messageId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/chat/messages/${messageId}`
    ).pipe(map(response => response.data));
  }
}