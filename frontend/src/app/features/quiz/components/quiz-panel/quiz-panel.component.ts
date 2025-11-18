import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Quiz, QuizState, QuizQuestion } from '../../../../core/models/chat.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-quiz-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent],
  templateUrl: './quiz-panel.component.html',
  styleUrls: ['./quiz-panel.component.css']
})
export class QuizPanelComponent implements OnChanges {
  @Input() public quiz: Quiz | null = null;
  @Input() public quizState: QuizState | null = null;
  @Input() public isLoading = false;
  @Output() public answerSubmit = new EventEmitter<string>();
  @Output() public nextQuestion = new EventEmitter<void>();
  @Output() public restartQuiz = new EventEmitter<void>();
  @Output() public finishQuiz = new EventEmitter<void>();

  public selectedOption: string | null = null;

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes['quizState'] || changes['quiz']) {
      this.selectedOption = null;
    }
  }

  public get currentQuestion(): QuizQuestion | null {
    if (!this.quiz || !this.quizState) return null;
    return this.quiz.questions[this.quizState.currentQuestionIndex];
  }

  public get isSubmitted(): boolean {
    return this.quizState?.isSubmitted[this.quizState.currentQuestionIndex] || false;
  }

  public get userAnswer(): string | null {
    return this.quizState?.userAnswers[this.quizState.currentQuestionIndex] || null;
  }

  public onOptionSelect(option: string): void {
    if (!this.isSubmitted) {
      this.selectedOption = option;
    }
  }

  public onSubmit(): void {
    if (this.selectedOption) {
      this.answerSubmit.emit(this.selectedOption);
    }
  }

  public onNext(): void {
    this.nextQuestion.emit();
  }

  public onFinish(): void {
    this.finishQuiz.emit();
  }

  public onRestart(): void {
    this.restartQuiz.emit();
  }

  public getOptionClasses(option: string): string {
    const baseClasses = 'flex items-center p-4 rounded-lg border-2 cursor-pointer transition-all';
    
    if (!this.isSubmitted) {
      return `${baseClasses} border-base-300 hover:border-brand-primary`;
    }
    
    if (option === this.currentQuestion?.correctAnswer) {
      return `${baseClasses} border-green-500 bg-green-500/10`;
    }
    
    if (option === this.userAnswer && option !== this.currentQuestion?.correctAnswer) {
      return `${baseClasses} border-red-500 bg-red-500/10`;
    }
    
    return `${baseClasses} border-base-300`;
  }

  public getProgressPercentage(): number {
    if (!this.quiz || !this.quizState) return 0;
    return ((this.quizState.currentQuestionIndex + 1) / this.quiz.questions.length) * 100;
  }
}