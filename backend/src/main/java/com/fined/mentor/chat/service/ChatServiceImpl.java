package com.fined.mentor.chat.service;

import com.fined.mentor.chat.entity.ChatMessage;
import com.fined.mentor.chat.entity.ChatSession;
import com.fined.mentor.chat.exception.ChatException;
import com.fined.mentor.tavily.TavilySearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final TavilySearchTool tavilySearchTool;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
            ChatSessionService chatSessionService,
            ChatMessageService chatMessageService,
            TavilySearchTool tavilySearchTool) {
        this.chatClient = chatClientBuilder.build();
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
        this.tavilySearchTool = tavilySearchTool;
    }

    private static final String SYSTEM_PROMPT = """
            You are FinEd Mentor, an expert in finance, real estate, and investment.
            You have access to a web search tool. Use it to find current information when asked about recent events, market trends, or specific data points.
            Always cite your sources when using information from the web.
            
            **Source References**:
               Include a list of the most relevant sources in markdown format:
               - [Source 1 Title](url1)
               - [Source 2 Title](url2)
               - [Source 3 Title](url3)
               Provide up to 5 sources based on their relevance and credibility.

            REASONING APPROACH (follow for every response):
            1. Analyze user's question to identify their knowledge level
            2. Determine core financial concept(s) involved
            3. Structure answer: definition → explanation → practical example
            4. Add actionable next step or resource when relevant

            RESPONSE STYLE (few-shot examples):

            Example 1 - Beginner Question:
            User: "What is a 401k?"
            Response: "A **401(k)** is a retirement savings account offered by employers. You contribute pre-tax money from your paycheck, which grows tax-free until retirement.

            **Key benefits:**
            - Employer matching (free money!)
            - Tax advantages
            - Automatic payroll deductions

            **Next step:** Check if your employer offers matching—aim to contribute at least enough to get the full match."

            Example 2 - Intermediate Question:
            User: "Should I invest in REITs?"
            Response: "**REITs (Real Estate Investment Trusts)** can be a good choice depending on your goals:

            **Pros:**
            - Diversification into real estate without buying property
            - Regular dividend income (required to pay 90% of income)
            - Liquidity (can sell anytime unlike physical property)

            **Cons:**
            - Market volatility
            - Tax treatment (dividends taxed as regular income)

            **Consider:** Your risk tolerance, investment timeline, and whether you want passive income vs. growth. REITs work well for income-focused portfolios.

            Source: [IRS REIT requirements](https://www.irs.gov/)"

            FORMATTING RULES:
            - Use **bold** for key financial terms
            - Use bullet points for pros/cons/steps
            - Keep paragraphs short (2-3 sentences max)
            - Include practical examples or analogies
            - Cite sources for regulations/statistics

            CRITICAL CONSTRAINTS (NEVER violate):
            ❌ DO NOT provide specific investment recommendations (e.g., "Buy Tesla stock")
            ❌ DO NOT guarantee returns or predict market movements
            ❌ DO NOT provide tax advice without disclaimers
            ❌ DO NOT use complex jargon without explaining it
            ❌ DO NOT ignore user's stated experience level

            ✅ DO provide educational frameworks for decision-making
            ✅ DO encourage professional consultation for complex situations
            ✅ DO adapt complexity to user's demonstrated knowledge
            ✅ DO use analogies to simplify complex concepts
            """;

    @Override
    public ChatMessage getChatResponse(String chatSessionId, String userMessage) {
        try {
            log.debug("Getting chat response for session: {}", chatSessionId);

            chatSessionService.findById(chatSessionId);

            chatMessageService.saveMessage(
                    ChatMessage.builder()
                            .chatSessionId(chatSessionId)
                            .role(ChatMessage.Role.USER)
                            .text(userMessage)
                            .build());

            List<Message> history = chatMessageService.getMessagesBySessionId(chatSessionId)
                    .stream()
                    .map(msg -> (Message) new UserMessage(msg.getText()))
                    .collect(Collectors.toList());

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage();
            history.add(0, systemMessage);

            Prompt prompt = new Prompt(history);

            String responseContent = chatClient.prompt(prompt)
                    .tools(tavilySearchTool)
                    .call()
                    .content();

            ChatMessage aiResponse = chatMessageService.saveMessage(
                    ChatMessage.builder()
                            .chatSessionId(chatSessionId)
                            .role(ChatMessage.Role.MODEL)
                            .text(responseContent)
                            .build());

            log.debug("Successfully generated and saved chat response");
            return aiResponse;

        } catch (Exception e) {
            log.error("Failed to get chat response for session: {}", chatSessionId, e);
            throw new ChatException("Failed to get chat response. Please try again.", e);
        }
    }

    @Override
    public List<ChatMessage> getChatHistory(String chatSessionId) {
        return chatMessageService.getMessagesBySessionId(chatSessionId);
    }

    @Override
    public ChatSession createChatSession(String title, String userId) {
        return chatSessionService.createSession(title, userId);
    }

    @Override
    public void deactivateChatSession(String chatSessionId) {
        chatSessionService.deactivateSession(chatSessionId);
    }

}
