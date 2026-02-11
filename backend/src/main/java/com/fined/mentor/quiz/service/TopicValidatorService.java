package com.fined.mentor.quiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicValidatorService {

    private final ChatClient.Builder chatClientBuilder;

    // Keywords that indicate finance-related topics
    private static final List<String> FINANCE_KEYWORDS = Arrays.asList(
            // English
            "finance", "investment", "stock", "bond", "portfolio", "trading", "dividend",
            "interest", "loan", "mortgage", "credit", "debt", "budget", "savings", "retirement",
            "401k", "ira", "roth", "pension", "etf", "mutual fund", "asset", "liability",
            "equity", "capital", "revenue", "profit", "loss", "tax", "banking", "insurance",
            "real estate", "property", "reit", "cryptocurrency", "forex", "commodity",
            // French
            "investissement", "actions", "obligations", "portefeuille", "épargne", "retraite",
            "crédit", "prêt", "hypothèque", "intérêt", "impôt", "banque", "assurance",
            "immobilier", "propriété", "bourse",
            // German
            "investition", "aktien", "anleihen", "sparplan", "rente", "kredit", "darlehen",
            "hypothek", "zinsen", "steuer", "versicherung", "immobilien", "eigentum", "börse");

    /**
     * Validates if a topic is related to finance, investment, or real estate.
     * Uses both keyword matching and AI validation for comprehensive checking.
     *
     * @param topic The topic to validate
     * @return true if the topic is valid (finance-related), false otherwise
     */
    public boolean isValidTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return false;
        }

        String normalizedTopic = topic.toLowerCase().trim();

        // First, do a quick keyword check
        boolean hasFinanceKeyword = FINANCE_KEYWORDS.stream()
                .anyMatch(normalizedTopic::contains);

        if (hasFinanceKeyword) {
            log.debug("Topic '{}' validated via keyword matching", topic);
            return true;
        }

        // If no keyword match, use AI to validate
        return validateWithAI(topic);
    }

    /**
     * Uses AI to validate if a topic is related to finance, investment, or real
     * estate.
     * This is a fallback for topics that don't match keywords but might still be
     * valid.
     *
     * @param topic The topic to validate
     * @return true if AI determines the topic is finance-related, false otherwise
     */
    private boolean validateWithAI(String topic) {
        try {
            ChatClient chatClient = chatClientBuilder.build();

            String validationPrompt = String.format("""
                    You are a topic classifier. Determine if the following topic is related to:
                    - Finance (personal finance, corporate finance, financial planning, banking)
                    - Investment (stocks, bonds, ETFs, mutual funds, portfolio management, trading)
                    - Real Estate (property investment, real estate markets, rental properties, mortgages)
                    - Immobilien (German real estate, property management)

                    Topic: "%s"

                    Respond with ONLY "YES" if the topic is related to any of the above domains.
                    Respond with ONLY "NO" if the topic is NOT related to any of the above domains.

                    Do not provide any explanation, just YES or NO.
                    """, topic);

            String response = chatClient.prompt(validationPrompt)
                    .call()
                    .content()
                    .trim()
                    .toUpperCase();

            boolean isValid = response.contains("YES");
            log.debug("Topic '{}' AI validation result: {}", topic, isValid ? "VALID" : "INVALID");
            return isValid;

        } catch (Exception e) {
            log.error("Error validating topic with AI: {}", topic, e);
            // In case of error, be conservative and reject the topic
            return false;
        }
    }

    /**
     * Gets a user-friendly error message in the appropriate language
     * based on the topic's language.
     *
     * @param topic The invalid topic
     * @return Localized error message
     */
    public String getInvalidTopicMessage(String topic) {
        // Detect language based on common words
        String lowerTopic = topic.toLowerCase();

        // French detection
        if (containsFrenchWords(lowerTopic)) {
            return "Le sujet '" + topic + "' n'est pas lié à la finance, l'investissement ou l'immobilier. " +
                    "Veuillez choisir un sujet dans ces domaines.";
        }

        // German detection
        if (containsGermanWords(lowerTopic)) {
            return "Das Thema '" + topic + "' bezieht sich nicht auf Finanzen, Investitionen oder Immobilien. " +
                    "Bitte wählen Sie ein Thema aus diesen Bereichen.";
        }

        // Default to English
        return "The topic '" + topic + "' is not related to finance, investment, or real estate. " +
                "Please choose a topic within these domains.";
    }

    private boolean containsFrenchWords(String text) {
        String[] frenchIndicators = { "le", "la", "les", "un", "une", "des", "est", "sont", "dans", "sur", "avec" };
        return Arrays.stream(frenchIndicators).anyMatch(text::contains);
    }

    private boolean containsGermanWords(String text) {
        String[] germanIndicators = { "der", "die", "das", "ein", "eine", "ist", "sind", "und", "über", "für" };
        return Arrays.stream(germanIndicators).anyMatch(text::contains);
    }
}
