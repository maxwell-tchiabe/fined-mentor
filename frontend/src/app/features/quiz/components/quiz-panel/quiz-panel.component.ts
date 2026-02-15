import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Quiz, QuizState, QuizQuestion, QuizStreamingProgress } from '../../../../core/models/chat.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-quiz-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, TranslateModule],
  templateUrl: './quiz-panel.component.html',
  styleUrls: ['./quiz-panel.component.css']
})
export class QuizPanelComponent implements OnChanges, OnInit, OnDestroy {
  @Input() public quiz: Quiz | null = null;
  @Input() public quizState: QuizState | null = null;
  @Input() public isLoading = false;
  @Input() public streamingProgress: QuizStreamingProgress = {
    elapsedTime: 0,
    status: 'GENERATING',
    charsReceived: 0,
    isGenerating: false,
    startedAt: null,
    firstChunkAt: null
  };
  @Output() public answerSubmit = new EventEmitter<string>();
  @Output() public nextQuestion = new EventEmitter<void>();
  @Output() public previousQuestion = new EventEmitter<void>();
  @Output() public restartQuiz = new EventEmitter<void>();
  @Output() public finishQuiz = new EventEmitter<void>();

  public selectedOption: string | null = null;
  public windowWidth = window.innerWidth;
  public displayProgress = 0;
  private progressInterval: any;

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.windowWidth = window.innerWidth;
  }

  public ngOnInit(): void {
    this.startProgressAnimation();
  }

  public ngOnDestroy(): void {
    if (this.progressInterval) {
      clearInterval(this.progressInterval);
    }
  }

  // Clean Code: Algorithm Constants
  private readonly PROGRESS_CONFIG = {
    UPDATE_INTERVAL_MS: 50,
    SAVING_STEP: 0.8,
    SMOOTHING_FACTOR: 0.05,
    WARMUP_MAX: 12,
    WARMUP_DURATION_MS: 5000,
    EXPECTED_CHARS: 4000,
    GENERATION_CAP: 92,
    TIME_BOOST_FACTOR: 0.15,
    TIME_BOOST_CAP: 8
  };

  private startProgressAnimation(): void {
    this.progressInterval = setInterval(() => {
      const target = this.getCircleProgress();
      this.updateDisplayProgress(target);
    }, this.PROGRESS_CONFIG.UPDATE_INTERVAL_MS);
  }

  private updateDisplayProgress(target: number): void {
    if (this.streamingProgress.status === 'SAVING') {
      this.animateToCompletion();
    } else {
      this.animateTowardsTarget(target);
    }
    this.displayProgress = Math.min(100, this.displayProgress);
  }

  private animateToCompletion(): void {
    if (this.displayProgress < 100) {
      this.displayProgress += this.PROGRESS_CONFIG.SAVING_STEP;
    }
  }

  private animateTowardsTarget(target: number): void {
    if (this.displayProgress < target) {
      const diff = target - this.displayProgress;
      this.displayProgress += Math.max(0.05, diff * this.PROGRESS_CONFIG.SMOOTHING_FACTOR);
    }
  }

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes['isLoading']?.currentValue === true) {
      this.displayProgress = 0;
    }
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

  public onPrevious(): void {
    this.previousQuestion.emit();
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

  public formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  public getCircleProgress(): number {
    const p = this.streamingProgress;
    if (p.status === 'SAVING') return 100;
    if (!this.isLoading || !p.startedAt) return 0;

    return p.firstChunkAt ? this.getStreamingPhaseProgress() : this.getWarmupPhaseProgress();
  }

  private getWarmupPhaseProgress(): number {
    const elapsed = Date.now() - (this.streamingProgress.startedAt || 0);
    const ratio = Math.min(1, elapsed / this.PROGRESS_CONFIG.WARMUP_DURATION_MS);
    return Math.max(2, ratio * this.PROGRESS_CONFIG.WARMUP_MAX);
  }

  private getStreamingPhaseProgress(): number {
    const p = this.streamingProgress;
    const volumeRatio = Math.min(1, p.charsReceived / this.PROGRESS_CONFIG.EXPECTED_CHARS);
    const volumeProgress = Math.sqrt(volumeRatio) * 75;

    const activeSeconds = (Date.now() - (p.firstChunkAt || 0)) / 1000;
    const timeBoost = Math.min(this.PROGRESS_CONFIG.TIME_BOOST_CAP, activeSeconds * this.PROGRESS_CONFIG.TIME_BOOST_FACTOR);

    return Math.min(this.PROGRESS_CONFIG.GENERATION_CAP, this.PROGRESS_CONFIG.WARMUP_MAX + volumeProgress + timeBoost);
  }
}
