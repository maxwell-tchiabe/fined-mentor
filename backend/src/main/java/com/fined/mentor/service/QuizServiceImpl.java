package com.fined.mentor.service;

import com.fined.mentor.entity.Quiz;
import com.fined.mentor.entity.QuizState;
import com.fined.mentor.repository.QuizRepository;
import com.fined.mentor.repository.QuizStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizStateRepository quizStateRepository;
    private final QuizGenerationService quizGenerationService;
    private final ChatSessionService chatSessionService;

    @Override
    public Quiz generateQuiz(String topic, String chatSessionId) {
        Quiz quiz = quizGenerationService.generateQuiz(topic);
        quiz.setChatSessionId(chatSessionId);
        return quizRepository.save(quiz);
    }

    @Override
    public QuizState startQuiz(String quizId, String chatSessionId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        QuizState quizState = new QuizState();
        quizState.setQuizId(quiz.getId());
        quizState.setChatSessionId(chatSessionId);
        quizState.setCurrentQuestionIndex(0);
        quizState.setUserAnswers(new HashMap<>());
        quizState.setFinished(false);
        return quizStateRepository.save(quizState);
    }

    @Override
    public QuizState submitAnswer(String quizStateId, int questionIndex, String answer) {
        QuizState quizState = getQuizState(quizStateId);
        quizState.getUserAnswers().put(questionIndex, answer);
        return quizStateRepository.save(quizState);
    }

    @Override
    public Quiz getQuizBySessionId(String sessionId) {
        return quizRepository.findByChatSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Quiz not found for this session"));
    }

    @Override
    public QuizState getQuizState(String quizStateId) {
        return quizStateRepository.findById(quizStateId)
                .orElseThrow(() -> new RuntimeException("Quiz state not found"));
    }

    @Override
    public QuizState getQuizStateBySessionId(String sessionId) {
        return quizStateRepository.findByChatSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Quiz state not found for this session"));
    }
}
