export interface ChatMessage {
  id?: string;
  role: 'USER' | 'MODEL';
  text: string;
  timestamp?: Date;
  chatSessionId?: string;
}

export interface ChatSession {
  id: string;
  title: string;
  createdAt: Date;
  messages: ChatMessage[];
  quiz: Quiz | null;
  quizState: QuizState | null;
  active: boolean;
}

export interface QuizQuestion {
  question: string;
  type: 'multiple-choice' | 'true-false';
  options: string[];
  correctAnswer: string;
  explanation: string;
}

export interface Quiz {
  id: string;
  topic: string;
  questions: QuizQuestion[];
  createdAt: Date;
  chatSessionId?: string;
}

export interface QuizState {
  id?: string;
  quizId: string;
  chatSessionId: string;
  currentQuestionIndex: number;
  userAnswers: Record<number, string>;
  isSubmitted: Record<number, boolean>;
  score: number;
  finished: boolean;
}
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
}