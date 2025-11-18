import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatSession } from '../../../../core/models/chat.model';

interface TopicStats {
  scores: number[];
  total: number[];
}

@Component({
  selector: 'app-dashboard-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-panel.component.html',
  styleUrls: ['./dashboard-panel.component.css']
})
export class DashboardPanelComponent implements OnChanges {
  @Input() sessions: ChatSession[] | null = [];

  quizzesTaken = 0;
  totalScore = 0;
  totalQuestions = 0;
  averageScore = 0;
  quizzesByTopic: Record<string, TopicStats> = {};

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sessions']) {
      this.calculateStats();
    }
  }

  private calculateStats(): void {
    const finishedSessions = (this.sessions || []).filter(s => s.quiz && s.quizState?.isFinished);

    this.quizzesTaken = finishedSessions.length;
    this.totalScore = finishedSessions.reduce((acc, s) => acc + (s.quizState?.score || 0), 0);
    this.totalQuestions = finishedSessions.reduce((acc, s) => acc + (s.quiz?.questions.length || 0), 0);
    this.averageScore = this.totalQuestions > 0 ? (this.totalScore / this.totalQuestions) * 100 : 0;

    this.quizzesByTopic = finishedSessions.reduce<Record<string, TopicStats>>((acc, session) => {
      const topic = session.quiz!.topic;
      if (!acc[topic]) {
        acc[topic] = { scores: [], total: [] };
      }
      acc[topic].scores.push(session.quizState!.score);
      acc[topic].total.push(session.quiz!.questions.length);
      return acc;
    }, {});
  }

  getTopicAverage(topic: string): number {
    const stats = this.quizzesByTopic[topic];
    if (!stats) return 0;
    
    const topicTotalScore = stats.scores.reduce((a, b) => a + b, 0);
    const topicTotalQuestions = stats.total.reduce((a, b) => a + b, 0);
    return topicTotalQuestions > 0 ? (topicTotalScore / topicTotalQuestions) * 100 : 0;
  }

  getTopicScore(topic: string): { score: number; total: number } {
    const stats = this.quizzesByTopic[topic];
    if (!stats) return { score: 0, total: 0 };
    
    const score = stats.scores.reduce((a, b) => a + b, 0);
    const total = stats.total.reduce((a, b) => a + b, 0);
    return { score, total };
  }

  objectKeys(obj: any): string[] {
  return Object.keys(obj);
}
}