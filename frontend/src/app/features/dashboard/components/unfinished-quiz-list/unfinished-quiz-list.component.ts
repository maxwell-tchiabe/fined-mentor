import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatSession } from '../../../../core/models/chat.model';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-unfinished-quiz-list',
    standalone: true,
    imports: [CommonModule, TranslateModule],
    templateUrl: './unfinished-quiz-list.component.html',
    styleUrls: ['./unfinished-quiz-list.component.css']
})
export class UnfinishedQuizListComponent {
    public unfinishedSessions: ChatSession[] = [];

    @Input() set sessions(value: ChatSession[] | null) {
        this.unfinishedSessions = (value || []).filter(s =>
            s.quiz && (!s.quizState || !s.quizState.finished)
        );
    }

    constructor(private router: Router) { }

    public resumeQuiz(sessionId: string): void {
        this.router.navigate(['/chat', sessionId]);
    }

    public getProgress(session: ChatSession): number {
        if (!session.quiz || !session.quizState) return 0;
        const total = session.quiz.questions.length;
        const current = session.quizState.currentQuestionIndex;
        return total > 0 ? (current / total) * 100 : 0;
    }

    public getRemainingQuestions(session: ChatSession): number {
        if (!session.quiz || !session.quizState) return 0;

        let completed = session.quizState.currentQuestionIndex;
        // If the current question is already submitted (answered correctly/incorrectly), count it as done
        const isCurrentSubmitted = session.quizState.isSubmitted &&
            session.quizState.isSubmitted[session.quizState.currentQuestionIndex];

        if (isCurrentSubmitted) {
            completed++;
        }

        return Math.max(0, session.quiz.questions.length - completed);
    }
}
