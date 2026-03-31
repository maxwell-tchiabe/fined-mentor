package com.fined.mentor.tavily;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TavilySearchToolTest {

    @Mock
    private TavilyApiClient tavilyApiClient;

    @InjectMocks
    private TavilySearchTool tavilySearchTool;

    @Test
    void testSearchWeb() {
        TavilyApiClient.TavilyResponse mockResponse = new TavilyApiClient.TavilyResponse();
        mockResponse.setAnswer("Test Answer");
        
        TavilyApiClient.TavilyResponse.Result mockResult = new TavilyApiClient.TavilyResponse.Result();
        mockResult.setTitle("Test Title");
        mockResult.setUrl("http://test.url");
        mockResult.setContent("Test Content");
        
        mockResponse.setResults(List.of(mockResult));

        when(tavilyApiClient.search(any(TavilyApiClient.TavilyRequest.class))).thenReturn(mockResponse);

        String result = tavilySearchTool.searchWeb("test query");

        assertTrue(result.contains("Test Answer"));
        assertTrue(result.contains("Test Title"));
        assertTrue(result.contains("http://test.url"));
        assertTrue(result.contains("Test Content"));
    }
}
