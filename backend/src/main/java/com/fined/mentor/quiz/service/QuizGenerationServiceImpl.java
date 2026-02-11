package com.fined.mentor.quiz.service;

import com.fined.mentor.quiz.dto.GeneratedQuizDTO;
import com.fined.mentor.quiz.entity.Quiz;
import com.fined.mentor.quiz.exception.QuizGenerationException;
import com.fined.mentor.quiz.exception.QuizValidationException;
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
  private final TopicValidatorService topicValidatorService;

  public QuizGenerationServiceImpl(ChatClient.Builder builder,
      TavilySearchTool tavilySearchTool,
      TopicValidatorService topicValidatorService) {
    this.chatClient = builder.build();
    this.tavilySearchTool = tavilySearchTool;
    this.topicValidatorService = topicValidatorService;
  }

  @Override
  public Quiz generateQuiz(String topic) {
    try {
      log.info("Generating quiz for topic: {}", topic);

      // Validate topic before generating quiz
      if (!topicValidatorService.isValidTopic(topic)) {
        String errorMessage = topicValidatorService.getInvalidTopicMessage(topic);
        log.warn("Invalid topic rejected: {}", topic);
        throw new QuizValidationException(errorMessage);
      }

      // Use BeanOutputConverter with DTO to avoid ID generation issues
      BeanOutputConverter<GeneratedQuizDTO> outputConverter = new BeanOutputConverter<>(GeneratedQuizDTO.class);

      String promptString = """
          IDENTITY: You are **Fined Mentor**, a specialized financial education assistant.

          TOPIC VALIDATION (CRITICAL):
          ⚠️ You ONLY generate quizzes for topics related to:
          - Finance (personal finance, corporate finance, financial planning, banking)
          - Investment (stocks, bonds, ETFs, mutual funds, portfolio management, trading)
          - Real Estate (property investment, real estate markets, rental properties, mortgages)
          - Immobilien (German real estate, property management, German market specifics)

          ❌ If the topic "{topic}" is NOT related to finance/investment/real estate/immobilien:
          You MUST throw an error or return an empty quiz. DO NOT generate quizzes for unrelated topics.

          LANGUAGE DETECTION (CRITICAL):
          🌍 Detect the language of the topic "{topic}" and generate the ENTIRE quiz in that language:
          - If topic is in French → Generate all questions, options, and explanations in French
          - If topic is in English → Generate all questions, options, and explanations in English
          - If topic is in German → Generate all questions, options, and explanations in German

          TASK: Generate a 5-question beginner quiz on "{topic}" (finance/real estate/investment domain).
          You have access to a web search tool. Use it to find current information if the topic relates to recent events or trends.

          CRITICAL OUTPUT FORMAT REQUIREMENTS:
          - You MUST strictly follow the JSON schema provided in the format section
          - EVERY question MUST have ALL fields populated: question, type, options, correctAnswer, explanation
          - The "options" field is MANDATORY for ALL question types (both MULTIPLE_CHOICE and TRUE_FALSE)
          - NEVER leave the "options" array empty or null
          - For MULTIPLE_CHOICE: provide exactly 4 options as strings in an array
          - For TRUE_FALSE: provide exactly 2 options as strings in the detected language
            * English: ["True", "False"]
            * French: ["Vrai", "Faux"]
            * German: ["Wahr", "Falsch"]
          - The "correctAnswer" MUST be one of the strings from the "options" array

          REASONING STEPS:
          1. VERIFY the topic is about finance/investment/real estate/immobilien
          2. DETECT the language of the topic
          3. Identify 5 core concepts beginners should know about "{topic}"
          4. For each concept, determine if multiple-choice or true/false fits best
          5. Ensure balanced difficulty progression (easiest to moderate)
          6. Write 1-2 sentence explanations using simple terms IN THE DETECTED LANGUAGE
          7. VALIDATE that every question has all required fields with proper values

          EXAMPLES (DO NOT COPY CONTENT, FOLLOW STRUCTURE):

          English Multiple Choice Example:
          {{
            "question": "What is compound interest?",
            "type": "MULTIPLE_CHOICE",
            "options": ["Interest on interest earned", "Simple interest rate", "Bank fee structure", "Investment loss"],
            "correctAnswer": "Interest on interest earned",
            "explanation": "Compound interest grows your money faster because you earn interest on both your initial deposit and previously earned interest."
          }}

          English True/False Example:
          {{
            "question": "Diversification means putting all your money in one investment.",
            "type": "TRUE_FALSE",
            "options": ["True", "False"],
            "correctAnswer": "False",
            "explanation": "Diversification means spreading your investments across different assets to reduce risk, not concentrating them in one place."
          }}

          French Multiple Choice Example:
          {{
            "question": "Qu'est-ce que l'intérêt composé ?",
            "type": "MULTIPLE_CHOICE",
            "options": ["Intérêt sur intérêt gagné", "Taux d'intérêt simple", "Structure de frais bancaires", "Perte d'investissement"],
            "correctAnswer": "Intérêt sur intérêt gagné",
            "explanation": "L'intérêt composé fait croître votre argent plus rapidement car vous gagnez des intérêts sur votre dépôt initial et sur les intérêts précédemment gagnés."
          }}

          German Multiple Choice Example:
          {{
            "question": "Was ist Zinseszins?",
            "type": "MULTIPLE_CHOICE",
            "options": ["Zinsen auf erwirtschaftete Zinsen", "Einfacher Zinssatz", "Bankgebührenstruktur", "Anlageverlust"],
            "correctAnswer": "Zinsen auf erwirtschaftete Zinsen",
            "explanation": "Zinseszins lässt Ihr Geld schneller wachsen, weil Sie Zinsen sowohl auf Ihre ursprüngliche Einlage als auch auf zuvor verdiente Zinsen erhalten."
          }}

          REQUIREMENTS:
          - Question mix: 3 MULTIPLE_CHOICE + 2 TRUE_FALSE
          - Each MULTIPLE_CHOICE: exactly 4 distinct options IN THE DETECTED LANGUAGE
          - Each TRUE_FALSE: exactly 2 options in the appropriate language (True/False, Vrai/Faux, or Wahr/Falsch)
          - Each explanation: 1-2 sentences maximum IN THE DETECTED LANGUAGE
          - Vocabulary: 8th-grade reading level
          - correctAnswer MUST match one option exactly (case-sensitive)
          - ALL content (questions, options, explanations) in the SAME language as the topic

          AVOID:
          - Generating quizzes for non-finance/investment/real estate topics
          - Mixing languages within the quiz
          - Empty or null options arrays
          - Missing any required fields
          - Complex calculations or formulas
          - Jargon without definitions
          - Trick questions or ambiguous phrasing
          - Questions requiring current market data (unless using search tool)

          VALIDATION CHECKLIST (verify before responding):
          ✓ Topic is related to finance/investment/real estate/immobilien
          ✓ All content is in the same language as the topic
          ✓ All 5 questions have the "options" field populated
          ✓ MULTIPLE_CHOICE questions have exactly 4 options
          ✓ TRUE_FALSE questions have exactly 2 options in the correct language
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

    } catch (QuizValidationException e) {
      // Re-throw validation exceptions without wrapping them
      log.warn("Topic validation failed: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Failed to generate quiz for topic: {}", topic, e);
      throw new QuizGenerationException("Could not generate a valid quiz. Please try again.", e);
    }
  }

}
