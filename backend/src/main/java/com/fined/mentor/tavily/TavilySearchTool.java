package com.fined.mentor.tavily;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TavilySearchTool {

    private final TavilyApiClient tavilyApiClient;

    public TavilySearchTool(TavilyApiClient tavilyApiClient) {
        this.tavilyApiClient = tavilyApiClient;
    }

    @Tool(description = "Search the web for information using Tavily API. Use this tool when you need current information, market trends, or specific data points.")
    public String searchWeb(String query) {
        TavilyApiClient.TavilyRequest request = TavilyApiClient.TavilyRequest.builder()
                .query(query)
                .searchDepth("basic")
                .topic("general")
                .maxResults(5)
                .includeAnswer(true)
                .build();

        TavilyApiClient.TavilyResponse response = tavilyApiClient.search(request);

        StringBuilder result = new StringBuilder();
        if (response.getAnswer() != null) {
            result.append("Answer: ").append(response.getAnswer()).append("\n\n");
        }

        if (response.getResults() != null) {
            result.append("Sources:\n");
            for (TavilyApiClient.TavilyResponse.Result r : response.getResults()) {
                result.append("- Title: ").append(r.getTitle()).append("\n");
                result.append("  URL: ").append(r.getUrl()).append("\n");
                result.append("  Content: ").append(r.getContent()).append("\n\n");
            }
        }

        return result.toString();
    }
}
