package com.fined.mentor.tavily;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TavilyApiClientTest {

    @Test
    void testTavilyRequestDto() {
        TavilyApiClient.TavilyRequest request = TavilyApiClient.TavilyRequest.builder()
                .query("query")
                .apiKey("key")
                .searchDepth("basic")
                .topic("general")
                .days(3)
                .timeRange("day")
                .maxResults(5)
                .includeImages(true)
                .includeImageDescriptions(true)
                .includeAnswer(true)
                .includeRawContent(true)
                .includeDomains(List.of("dom1"))
                .excludeDomains(List.of("dom2"))
                .build();

        assertEquals("query", request.getQuery());
        assertEquals("key", request.getApiKey());
        assertEquals("basic", request.getSearchDepth());
        assertEquals("general", request.getTopic());
        assertEquals(3, request.getDays());
        assertEquals("day", request.getTimeRange());
        assertEquals(5, request.getMaxResults());
        assertTrue(request.isIncludeImages());
        assertTrue(request.isIncludeImageDescriptions());
        assertTrue(request.isIncludeAnswer());
        assertTrue(request.isIncludeRawContent());
        assertEquals(1, request.getIncludeDomains().size());
        assertEquals(1, request.getExcludeDomains().size());
    }

    @Test
    void testTavilyResponseDto() {
        TavilyApiClient.TavilyResponse response = new TavilyApiClient.TavilyResponse();
        response.setQuery("query");
        response.setFollowUpQuestions(List.of("q1"));
        response.setAnswer("answer");
        response.setResponseTime(0.5f);

        TavilyApiClient.TavilyResponse.Image image = new TavilyApiClient.TavilyResponse.Image("url", "desc");
        response.setImages(List.of(image));

        TavilyApiClient.TavilyResponse.Result result = new TavilyApiClient.TavilyResponse.Result("title", "url", "content", "raw", 0.9f, "date");
        response.setResults(List.of(result));

        assertEquals("query", response.getQuery());
        assertEquals("answer", response.getAnswer());
        assertEquals(0.5f, response.getResponseTime());
        assertEquals(1, response.getImages().size());
        assertEquals(1, response.getResults().size());
    }
}
