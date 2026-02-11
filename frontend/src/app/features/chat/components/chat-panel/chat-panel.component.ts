import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatMessage } from '../../../../core/models/chat.model';
import { MarkdownPipe } from '../../../../shared/pipes/markdown.pipe';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-chat-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownPipe, LoadingSpinnerComponent, TranslateModule],
  templateUrl: './chat-panel.component.html',
  styleUrls: ['./chat-panel.component.css']
})
export class ChatPanelComponent implements OnChanges {
  @Input() public messages: ChatMessage[] = [];
  @Input() public isLoading = false;
  @Output() public sendMessage = new EventEmitter<string>();
  @Output() public generateQuiz = new EventEmitter<string>();

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  public messageInput = '';
  public isQuizMode = false;

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes['messages']) {
      this.scrollToBottom();
    }
  }

  public toggleQuizMode(): void {
    this.isQuizMode = !this.isQuizMode;
    this.messageInput = ''; // Clear input when switching modes
  }

  public onSubmit(): void {
    if (this.messageInput.trim() && !this.isLoading) {
      if (this.isQuizMode) {
        this.generateQuiz.emit(this.messageInput.trim());
      } else {
        this.sendMessage.emit(this.messageInput.trim());
      }
      this.messageInput = '';
    }
  }

  public onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSubmit();
    }
  }

  public autoResize(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop =
          this.messagesContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }

  public trackByMessage(index: number, message: ChatMessage): string {
    return message.id || `${index}-${message.role}-${message.text.substring(0, 10)}`;
  }

  public getMessageClasses(msg: ChatMessage): string {
    const baseClasses = 'flex items-start gap-3';
    return msg.role === 'USER' ? `${baseClasses} flex-row-reverse` : baseClasses;
  }

  public getBubbleClasses(msg: ChatMessage): string {
    const baseClasses = 'max-w-md lg:max-w-lg px-4 py-3 rounded-2xl';
    return msg.role === 'USER'
      ? `${baseClasses} bg-brand-500 text-content-200 rounded-br-none`
      : `${baseClasses} bg-base-300 text-content-200 rounded-bl-none`;
  }
}