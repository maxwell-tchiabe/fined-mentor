package com.fined.mentor.quiz.service;

import com.fined.mentor.chat.service.ChatSessionService;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizQuestion;
import com.fined.mentor.quiz.entity.QuizState;
import com.fined.mentor.quiz.exception.*;
import com.fined.mentor.quiz.repository.QuizRepository;
import com.fined.mentor.quiz.repository.QuizStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizStateRepository quizStateRepository;
    private final QuizGenerationService quizGenerationService;
    private final ChatSessionService chatSessionService;

    @Override
    @Transactional
    public Quiz generateQuiz(String topic, String chatSessionId) {
        try {
            log.info("Generating quiz for topic: {} and session: {}", topic, chatSessionId);

            Quiz quiz = quizGenerationService.generateQuiz(topic);
            quiz.setChatSessionId(chatSessionId);
            quiz.setCreatedAt(LocalDateTime.now());

            // Validate quiz structure
            validateQuiz(quiz);

            Quiz savedQuiz = quizRepository.save(quiz);
            log.debug("Successfully saved quiz with id: {} and {} questions",
                    savedQuiz.getId(), savedQuiz.getQuestions().size());

            return savedQuiz;

        } catch (Exception e) {
            log.error("Failed to generate quiz for topic: {} and session: {}", topic, chatSessionId, e);
            throw new QuizGenerationException("Failed to generate quiz. Please try again.", e);
        }
    }

    @Override
    @Transactional
    public QuizState submitAnswer(String quizStateId, int questionIndex, String answer) {
        try {
            log.debug("Submitting answer for quiz state: {}, question: {}, answer: {}",
                    quizStateId, questionIndex, answer);

            QuizState quizState = quizStateRepository.findById(quizStateId)
                    .orElseThrow(() -> new QuizStateNotFoundException("Quiz state not found with id: " + quizStateId));

            validateAnswerSubmission(quizState, questionIndex, answer);

            // Update user answer (maps keyed by question index)
            quizState.getUserAnswers().put(questionIndex, answer);

            // Calculate and update score if not already submitted
            Boolean alreadySubmitted = quizState.getIsSubmitted().get(questionIndex);
            if (alreadySubmitted == null || !alreadySubmitted) {
                int newScore = calculateScore(quizState, questionIndex, answer);
                quizState.setScore(newScore);
                quizState.getIsSubmitted().put(questionIndex, true);

                log.debug("Score updated to: {} for quiz state: {}", newScore, quizStateId);
            }

            // Let the frontend control the question index progression
            // updateCurrentQuestionIndex(quizState);

            // Let the frontend control when the quiz is finished
            // checkAndMarkQuizFinished(quizState);

            QuizState updatedState = quizStateRepository.save(quizState);
            log.debug("Answer submitted successfully for quiz state: {}", quizStateId);

            return updatedState;

        } catch (Exception e) {
            log.error("Failed to submit answer for quiz state: {}, question: {}", quizStateId, questionIndex, e);
            throw new QuizException("Failed to submit answer. Please try again.");
        }
    }

    @Transactional
    @Override
    public QuizState finishQuiz(String quizStateId) {
        try {
            log.info("Finishing quiz state: {}", quizStateId);

            QuizState quizState = quizStateRepository.findById(quizStateId)
                    .orElseThrow(() -> new QuizStateNotFoundException("Quiz state not found with id: " + quizStateId));

            // Calculate final score including all questions
            int finalScore = calculateFinalScore(quizState);
            quizState.setScore(finalScore);
            quizState.setFinished(true);

            QuizState finishedState = quizStateRepository.save(quizState);
            log.info("Quiz finished with final score: {}/{} for quiz state: {}",
                    finalScore, getTotalQuestions(quizState), quizStateId);

            return finishedState;

        } catch (Exception e) {
            log.error("Failed to finish quiz state: {}", quizStateId, e);
            throw new QuizException("Failed to finish quiz. Please try again.");
        }
    }

    // ========== SCORING LOGIC ==========

    private int calculateScore(QuizState quizState, int questionIndex, String answer) {
        Quiz quiz = quizRepository.findById(quizState.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found"));

        QuizQuestion question = quiz.getQuestions().get(questionIndex);
        String correctAnswer = question.getCorrectAnswer();

        boolean isCorrect = isAnswerCorrect(answer, correctAnswer, question.getType());

        if (isCorrect) {
            return quizState.getScore() + 1;
        }
        return quizState.getScore();
    }

    private int calculateFinalScore(QuizState quizState) {
        Quiz quiz = quizRepository.findById(quizState.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found"));

        int score = 0;
        List<QuizQuestion> questions = quiz.getQuestions();
        // userAnswers is a map: Integer -> String
        for (int i = 0; i < questions.size(); i++) {
            String userAns = quizState.getUserAnswers().get(i);
            if (userAns != null) {
                boolean isCorrect = isAnswerCorrect(
                        userAns,
                        questions.get(i).getCorrectAnswer(),
                        questions.get(i).getType());
                if (isCorrect) {
                    score++;
                }
            }
        }

        return score;
    }

    private boolean isAnswerCorrect(String userAnswer, String correctAnswer, QuizQuestion.QuestionType type) {
        if (userAnswer == null) {
            return false;
        }

        switch (type) {
            case MULTIPLE_CHOICE:
                // For multiple choice, compare exact strings
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());

            case TRUE_FALSE:
                // For true/false, be more flexible with input
                String normalizedUserAnswer = normalizeTrueFalseAnswer(userAnswer);
                String normalizedCorrectAnswer = normalizeTrueFalseAnswer(correctAnswer);
                return normalizedUserAnswer.equals(normalizedCorrectAnswer);

            default:
                log.warn("Unknown question type: {}", type);
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
    }

    private String normalizeTrueFalseAnswer(String answer) {
        if (answer == null)
            return "";

        String normalized = answer.trim().toLowerCase();
        if (normalized.startsWith("t") || normalized.equals("yes") || normalized.equals("y")
                || normalized.equals("1")) {
            return "true";
        } else if (normalized.startsWith("f") || normalized.equals("no") || normalized.equals("n")
                || normalized.equals("0")) {
            return "false";
        }
        return normalized;
    }

    // ========== VALIDATION METHODS ==========

    private void validateQuiz(Quiz quiz) {
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new QuizValidationException("Quiz must have at least one question");
        }

        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            QuizQuestion question = quiz.getQuestions().get(i);
            validateQuestion(question, i);
        }
    }

    private void validateQuestion(QuizQuestion question, int index) {
        if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) {
            throw new QuizValidationException("Question text is required for question " + (index + 1));
        }

        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            throw new QuizValidationException("Correct answer is required for question " + (index + 1));
        }

        if (question.getType() == QuizQuestion.QuestionType.MULTIPLE_CHOICE) {
            if (question.getOptions() == null || question.getOptions().length < 2) {
                throw new QuizValidationException(
                        "Multiple choice questions must have at least 2 options for question " + (index + 1));
            }

            // Verify correct answer is among options
            boolean correctAnswerFound = false;
            for (String option : question.getOptions()) {
                if (option != null && option.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                    correctAnswerFound = true;
                    break;
                }
            }

            if (!correctAnswerFound) {
                throw new QuizValidationException(
                        "Correct answer must be one of the provided options for question " + (index + 1));
            }
        } else if (question.getType() == QuizQuestion.QuestionType.TRUE_FALSE) {
            if (!"true".equalsIgnoreCase(question.getCorrectAnswer().trim()) &&
                    !"false".equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                throw new QuizValidationException(
                        "Correct answer for TRUE_FALSE question must be either 'true' or 'false' for question " + (index + 1));
            }

            // Ensure correct answer matches one of the TRUE_FALSE options
            boolean correctAnswerFound = false;
            for (String option : question.getOptions()) {
                if (option != null && option.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                    correctAnswerFound = true;
                    break;
                }
            }

            if (!correctAnswerFound) {
                throw new QuizValidationException(
                        "Correct answer must match one of the TRUE_FALSE options for question " + (index + 1));
            }
        }
    }

    private void validateAnswerSubmission(QuizState quizState, int questionIndex, String answer) {
        if (quizState.isFinished()) {
            throw new QuizException("Cannot submit answer - quiz is already finished");
        }

        Quiz quiz = quizRepository.findById(quizState.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found"));

        int totalQuestions = quiz.getQuestions().size();
        if (questionIndex < 0 || questionIndex >= totalQuestions) {
            throw new QuizException("Invalid question index: " + questionIndex);
        }

        QuizQuestion question = quiz.getQuestions().get(questionIndex);

        // Validate answer format based on question type
        if (question.getType() == QuizQuestion.QuestionType.TRUE_FALSE && answer != null) {
            String normalizedAnswer = normalizeTrueFalseAnswer(answer);
            if (!normalizedAnswer.equals("true") && !normalizedAnswer.equals("false")) {
                throw new QuizValidationException("Answer must be 'true' or 'false' for true/false questions");
            }
        }
    }

    // ========== HELPER METHODS ==========

    private void updateCurrentQuestionIndex(QuizState quizState) {
        int nextIndex = quizState.getCurrentQuestionIndex() + 1;
        // Use quiz question count to determine bounds
        Quiz quiz = quizRepository.findById(quizState.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found"));
        int total = quiz.getQuestions().size();
        if (nextIndex < total) {
            quizState.setCurrentQuestionIndex(nextIndex);
        }
    }

    private void checkAndMarkQuizFinished(QuizState quizState) {
        Quiz quiz = quizRepository.findById(quizState.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found"));
        int total = quiz.getQuestions().size();

        boolean allAnswered = true;
        for (int i = 0; i < total; i++) {
            if (quizState.getUserAnswers().get(i) == null) {
                allAnswered = false;
                break;
            }
        }

        if (allAnswered) {
            quizState.setFinished(true);
            log.debug("All questions answered, marking quiz as finished: {}", quizState.getId());
        }
    }

    private int getTotalQuestions(QuizState quizState) {
        Quiz quiz = quizRepository.findById(quizState.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found"));
        return quiz.getQuestions().size();
    }

    @Override
    public QuizState startQuiz(String quizId, String chatSessionId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        QuizState quizState = new QuizState();
        quizState.setQuizId(quiz.getId());
        quizState.setChatSessionId(chatSessionId);
        quizState.setCurrentQuestionIndex(0);
        // Use maps for answers and submission flags (keys are question indices)
        quizState.setUserAnswers(new HashMap<>());
        quizState.setIsSubmitted(new HashMap<>());
        quizState.setScore(0);
        quizState.setFinished(false);
        return quizStateRepository.save(quizState);
    }

    @Override
    public Quiz getQuizBySessionId(String sessionId) {
        return quizRepository.findFirstByChatSessionIdOrderByCreatedAtDesc(sessionId)
                .orElseThrow(() -> new RuntimeException("Quiz not found for this session"));
    }

    @Override
    public QuizState getQuizState(String quizStateId) {
        return quizStateRepository.findById(quizStateId)
                .orElseThrow(() -> new RuntimeException("Quiz state not found"));
    }

    @Override
    public QuizState getQuizStateBySessionId(String sessionId) {
        return quizStateRepository.findFirstByChatSessionIdOrderByIdDesc(sessionId)
                .orElseThrow(() -> new RuntimeException("Quiz state not found for this session"));
    }
}
