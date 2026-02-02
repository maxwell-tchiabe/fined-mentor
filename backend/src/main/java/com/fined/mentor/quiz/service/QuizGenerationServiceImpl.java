package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.dto.GeneratedQuizDTO;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.exception.QuizGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import com.fined.mentor.tavily.TavilySearchTool;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
class QuizGenerationServiceImpl implements QuizGenerationService {

    private final ChatClient chatClient;
    private final TavilySearchTool tavilySearchTool;

    public QuizGenerationServiceImpl(ChatClient.Builder builder, TavilySearchTool tavilySearchTool) {
        this.chatClient = builder.build();
        this.tavilySearchTool = tavilySearchTool;
    }

    @Override
    public Quiz generateQuiz(String topic) {
        try {
            log.info("Generating quiz for topic: {}", topic);

            // Use BeanOutputConverter with DTO to avoid ID generation issues
            BeanOutputConverter<GeneratedQuizDTO> outputConverter = new BeanOutputConverter<>(GeneratedQuizDTO.class);

            String promptString = """
                    Generate a 5-question beginner quiz on "{topic}" (finance/real estate/investment domain).
                    You have access to a web search tool. Use it to find current information if the topic relates to recent events or trends.

                    CRITICAL OUTPUT FORMAT REQUIREMENTS:
                    - You MUST strictly follow the JSON schema provided in the format section
                    - EVERY question MUST have ALL fields populated: question, type, options, correctAnswer, explanation
                    - The "options" field is MANDATORY for ALL question types (both MULTIPLE_CHOICE and TRUE_FALSE)
                    - NEVER leave the "options" array empty or null
                    - For MULTIPLE_CHOICE: provide exactly 4 options as strings in an array
                    - For TRUE_FALSE: provide exactly 2 options as strings: ["True", "False"]
                    - The "correctAnswer" MUST be one of the strings from the "options" array

                    REASONING STEPS:
                    1. Identify 5 core concepts beginners should know about "{topic}"
                    2. For each concept, determine if multiple-choice or true/false fits best
                    3. Ensure balanced difficulty progression (easiest to moderate)
                    4. Write 1-2 sentence explanations using simple terms
                    5. VALIDATE that every question has all required fields with proper values

                    EXAMPLES (DO NOT COPY CONTENT, FOLLOW STRUCTURE):

                    Multiple Choice Example:
                    {{
                      "question": "What is compound interest?",
                      "type": "MULTIPLE_CHOICE",
                      "options": ["Interest on interest earned", "Simple interest rate", "Bank fee structure", "Investment loss"],
                      "correctAnswer": "Interest on interest earned",
                      "explanation": "Compound interest grows your money faster because you earn interest on both your initial deposit and previously earned interest."
                    }}

                    True/False Example:
                    {{
                      "question": "Diversification means putting all your money in one investment.",
                      "type": "TRUE_FALSE",
                      "options": ["True", "False"],
                      "correctAnswer": "False",
                      "explanation": "Diversification means spreading your investments across different assets to reduce risk, not concentrating them in one place."
                    }}

                    REQUIREMENTS:
                    - Question mix: 3 MULTIPLE_CHOICE + 2 TRUE_FALSE
                    - Each MULTIPLE_CHOICE: exactly 4 distinct options
                    - Each TRUE_FALSE: exactly ["True", "False"] as options
                    - Each explanation: 1-2 sentences maximum
                    - Vocabulary: 8th-grade reading level
                    - correctAnswer MUST match one option exactly (case-sensitive)

                    AVOID:
                    - Empty or null options arrays
                    - Missing any required fields
                    - Complex calculations or formulas
                    - Jargon without definitions
                    - Trick questions or ambiguous phrasing
                    - Questions requiring current market data (unless using search tool)

                    VALIDATION CHECKLIST (verify before responding):
                    ✓ All 5 questions have the "options" field populated
                    ✓ MULTIPLE_CHOICE questions have exactly 4 options
                    ✓ TRUE_FALSE questions have exactly ["True", "False"]
                    ✓ correctAnswer exists in the options array for each question
                    ✓ All fields (question, type, options, correctAnswer, explanation) are present

                    {format}
                    """;

            PromptTemplate promptTemplate = new PromptTemplate(promptString);
            Prompt prompt = promptTemplate.create(Map.of("topic", topic, "format", outputConverter.getFormat()));

            String content = chatClient.prompt(prompt)
                    .tools(tavilySearchTool)
                    .call()
                    .content();

            GeneratedQuizDTO generatedQuiz = outputConverter.convert(content);

            Quiz quiz = Quiz.builder()
                    .topic(generatedQuiz.getTopic())
                    .questions(generatedQuiz.getQuestions())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            log.debug("Successfully generated quiz with {} questions", quiz.getQuestions().size());

            return quiz;

        } catch (Exception e) {
            log.error("Failed to generate quiz for topic: {}", topic, e);
            throw new QuizGenerationException("Could not generate a valid quiz. Please try again.", e);
        }
    }

}
