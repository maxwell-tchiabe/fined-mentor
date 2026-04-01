package com.fined.mentor.quiz.service;

import com.fined.mentor.chat.service.ChatSessionService;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.entity.QuizQuestion;
import com.fined.mentor.quiz.entity.QuizState;
import com.fined.mentor.quiz.exception.QuizException;
import com.fined.mentor.quiz.exception.QuizValidationException;
import com.fined.mentor.quiz.repository.QuizRepository;
import com.fined.mentor.quiz.repository.QuizStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizStateRepository quizStateRepository;

    @Mock
    private QuizGenerationService quizGenerationService;

    @Mock
    private ChatSessionService chatSessionService;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Quiz sampleQuiz;
    private QuizState sampleQuizState;
    private QuizQuestion sampleQuestion;

    @BeforeEach
    void setUp() {
        sampleQuestion = new QuizQuestion();
        sampleQuestion.setQuestion("What is 401k?");
        sampleQuestion.setType(QuizQuestion.QuestionType.MULTIPLE_CHOICE);
        sampleQuestion.setOptions(new String[] { "Retirement", "Bank", "Loan", "Credit" });
        sampleQuestion.setCorrectAnswer("Retirement");
        sampleQuestion.setExplanation("It is a retirement account.");

        sampleQuiz = Quiz.builder()
                .id("quiz1")
                .topic("Investment")
                .chatSessionId("session1")
                .questions(Collections.singletonList(sampleQuestion))
                .build();

        sampleQuizState = new QuizState();
        sampleQuizState.setId("state1");
        sampleQuizState.setQuizId("quiz1");
        sampleQuizState.setChatSessionId("session1");
        sampleQuizState.setCurrentQuestionIndex(0);
        sampleQuizState.setUserAnswers(new HashMap<>());
        sampleQuizState.setIsSubmitted(new HashMap<>());
        sampleQuizState.setScore(0);
        sampleQuizState.setFinished(false);
    }

    @Test
    void generateQuiz_Success() {
        when(quizGenerationService.generateQuiz("Investment")).thenReturn(sampleQuiz);
        when(quizRepository.save(any(Quiz.class))).thenReturn(sampleQuiz);

        Quiz quiz = quizService.generateQuiz("Investment", "session1");

        assertNotNull(quiz);
        assertEquals("session1", quiz.getChatSessionId());
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void streamQuizGeneration_Success() {
        when(quizGenerationService.streamQuizGeneration("Investment")).thenReturn(Flux.just("quiz content"));

        Flux<String> stream = quizService.streamQuizGeneration("Investment");

        assertNotNull(stream);
    }

    @Test
    void submitAnswer_CorrectAnswer() {
        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));
        when(quizStateRepository.save(any(QuizState.class))).thenReturn(sampleQuizState);

        QuizState updatedState = quizService.submitAnswer("state1", 0, "Retirement");

        assertNotNull(updatedState);
        assertEquals("Retirement", updatedState.getUserAnswers().get(0));
        assertTrue(updatedState.getIsSubmitted().get(0));
        assertEquals(1, updatedState.getScore());
    }

    @Test
    void submitAnswer_IncorrectAnswer() {
        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));
        when(quizStateRepository.save(any(QuizState.class))).thenReturn(sampleQuizState);

        QuizState updatedState = quizService.submitAnswer("state1", 0, "Bank");

        assertNotNull(updatedState);
        assertEquals("Bank", updatedState.getUserAnswers().get(0));
        assertTrue(updatedState.getIsSubmitted().get(0));
        assertEquals(0, updatedState.getScore());
    }

    @Test
    void submitAnswer_AlreadyFinished() {
        sampleQuizState.setFinished(true);
        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));

        assertThrows(QuizException.class, () -> quizService.submitAnswer("state1", 0, "Retirement"));
    }

    @Test
    void finishQuiz_Success() {
        sampleQuizState.getUserAnswers().put(0, "Retirement");

        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));
        when(quizStateRepository.save(any(QuizState.class))).thenReturn(sampleQuizState);

        QuizState finishedState = quizService.finishQuiz("state1");

        assertNotNull(finishedState);
        assertTrue(finishedState.isFinished());
        assertEquals(1, finishedState.getScore());
    }

    @Test
    void startQuiz_Success() {
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));
        when(quizStateRepository.save(any(QuizState.class))).thenReturn(sampleQuizState);

        QuizState newState = quizService.startQuiz("quiz1", "session1");

        assertNotNull(newState);
        assertEquals("quiz1", newState.getQuizId());
    }

    @Test
    void getQuizBySessionId_Success() {
        when(quizRepository.findFirstByChatSessionIdOrderByCreatedAtDesc("session1"))
                .thenReturn(Optional.of(sampleQuiz));

        Quiz quiz = quizService.getQuizBySessionId("session1");

        assertNotNull(quiz);
        assertEquals("session1", quiz.getChatSessionId());
    }

    @Test
    void getQuizStateBySessionId_Success() {
        when(quizStateRepository.findFirstByChatSessionIdOrderByIdDesc("session1"))
                .thenReturn(Optional.of(sampleQuizState));

        QuizState state = quizService.getQuizStateBySessionId("session1");

        assertNotNull(state);
    }

    @Test
    void validateQuiz_InvalidQuestionEx() {
        sampleQuestion.setQuestion(null);
        when(quizGenerationService.generateQuiz("Investment")).thenReturn(sampleQuiz);

        assertThrows(QuizValidationException.class, () -> quizService.generateQuiz("Investment", "session1"));
    }

    @Test
    void saveStreamedQuiz_Success_WithMarkdown() {
        String json = "```json\n{\"topic\": \"Investment\", \"questions\": [{\"question\": \"Q1\", \"correctAnswer\": \"A1\", \"type\": \"MULTIPLE_CHOICE\", \"options\": [\"A1\", \"B2\"]}]}\n```";
        when(quizRepository.save(any(Quiz.class))).thenReturn(sampleQuiz);

        Quiz quiz = quizService.saveStreamedQuiz("Investment", "session1", json);

        assertNotNull(quiz);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void finishQuiz_PartialAnswers_CalculatesCorrectScore() {
        sampleQuizState.getUserAnswers().put(0, "Retirement");
        // No answer for a second question (if it existed)

        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));
        when(quizStateRepository.save(any(QuizState.class))).thenReturn(sampleQuizState);

        QuizState finishedState = quizService.finishQuiz("state1");

        assertEquals(1, finishedState.getScore());
    }

    @Test
    void testNormalizeTrueFalseAnswer() {
        sampleQuestion.setType(QuizQuestion.QuestionType.TRUE_FALSE);
        sampleQuestion.setCorrectAnswer("true");
        sampleQuestion.setOptions(new String[] { "true", "false" });

        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));
        when(quizStateRepository.save(any(QuizState.class))).thenReturn(sampleQuizState);

        QuizState state = quizService.submitAnswer("state1", 0, "vrai"); // French for true
        assertEquals(1, state.getScore());

        // Reset submission for next check or it won't increment
        sampleQuizState.getIsSubmitted().put(0, false);
        QuizState state2 = quizService.submitAnswer("state1", 0, "WAHR"); // German for true
        assertEquals(2, state2.getScore());
    }

    @Test
    void validateQuiz_MultipleChoice_CorrectAnswerNotInOptions_ThrowsException() {
        sampleQuestion.setType(QuizQuestion.QuestionType.MULTIPLE_CHOICE);
        sampleQuestion.setOptions(new String[] { "A", "B" });
        sampleQuestion.setCorrectAnswer("C");

        when(quizGenerationService.generateQuiz("Investment")).thenReturn(sampleQuiz);

        assertThrows(QuizValidationException.class, () -> quizService.generateQuiz("Investment", "session1"));
    }

    @Test
    void submitAnswer_InvalidIndex_ThrowsException() {
        when(quizStateRepository.findById("state1")).thenReturn(Optional.of(sampleQuizState));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(sampleQuiz));

        assertThrows(QuizException.class, () -> quizService.submitAnswer("state1", 99, "Ans"));
    }

    @Test
    void testNormalizeTrueFalseAnswer_AllVariants() {
        // True variants
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "true"));
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "VRAI"));
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "wahr "));
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "Yes"));
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "y"));
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "1"));
        assertEquals("true", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "Truth")); // starts
                                                                                                                  // with
                                                                                                                  // t

        // False variants
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "false"));
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "FAUX"));
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "falsch"));
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "No"));
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "n"));
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "0"));
        assertEquals("false", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "Fake")); // starts
                                                                                                                  // with
                                                                                                                  // f

        // Edge cases
        assertEquals("", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", (Object) null));
        assertEquals("unknown", ReflectionTestUtils.invokeMethod(quizService, "normalizeTrueFalseAnswer", "unknown"));
    }

    @Test
    void getQuizState_UnexpectedError() {
        // Use a generic Exception to trigger the catch block in QuizServiceImpl.getQuizState
        when(quizStateRepository.findById(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(QuizException.class, () -> quizService.getQuizState("state1"));
    }

    @Test
    void updateCurrentQuestionIndex_UnexpectedError() {
        // Use a generic Exception to trigger the catch block in QuizServiceImpl.updateCurrentQuestionIndex
        when(quizStateRepository.findById(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(QuizException.class, () -> quizService.updateCurrentQuestionIndex("state1", 1));
    }

    @Test
    void validateQuestion_UnexpectedError() {
        // This test catches unexpected errors in validateQuestion through generateQuiz
        QuizQuestion mockQuestion = mock(QuizQuestion.class);
        // Throw an exception during any of the validation steps (e.g. getType)
        when(mockQuestion.getQuestion()).thenReturn("What is 401k?");
        when(mockQuestion.getCorrectAnswer()).thenReturn("Retirement");
        when(mockQuestion.getType()).thenThrow(new RuntimeException("Unexpected error during validation"));

        Quiz quizWithMock = Quiz.builder()
                .questions(Collections.singletonList(mockQuestion))
                .build();

        when(quizGenerationService.generateQuiz(anyString())).thenReturn(quizWithMock);

        assertThrows(com.fined.mentor.quiz.exception.QuizGenerationException.class,
                () -> quizService.generateQuiz("Investment", "session1"));
    }
}
