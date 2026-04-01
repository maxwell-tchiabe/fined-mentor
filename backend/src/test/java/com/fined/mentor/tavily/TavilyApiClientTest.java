package com.fined.mentor.tavily;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TavilyApiClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private TavilyApiClient tavilyApiClient;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        tavilyApiClient = new TavilyApiClient(restClientBuilder, "test-api-key");
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_Success() {
        TavilyApiClient.TavilyRequest request = TavilyApiClient.TavilyRequest.builder()
                .query("test query")
                .build();

        TavilyApiClient.TavilyResponse mockResponse = new TavilyApiClient.TavilyResponse();
        mockResponse.setQuery("test query");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(TavilyApiClient.TavilyRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(TavilyApiClient.TavilyResponse.class)).thenReturn(mockResponse);

        TavilyApiClient.TavilyResponse response = tavilyApiClient.search(request);

        assertNotNull(response);
        assertEquals("test query", response.getQuery());
    }

    @Test
    void search_EmptyQuery_ThrowsException() {
        TavilyApiClient.TavilyRequest request = TavilyApiClient.TavilyRequest.builder()
                .query("")
                .build();

        assertThrows(IllegalArgumentException.class, () -> tavilyApiClient.search(request));
    }

    @Test
    void testImageDeserializer() throws Exception {
        String json = "{\"query\": \"test\", \"images\": [{\"url\": \"http://image1.com\", \"description\": \"desc1\"}, \"http://image2.com\"]}";
        ObjectMapper mapper = new ObjectMapper();
        
        TavilyApiClient.TavilyResponse response = mapper.readValue(json, TavilyApiClient.TavilyResponse.class);
        List<TavilyApiClient.TavilyResponse.Image> images = response.getImages();

        assertEquals(2, images.size());
        assertEquals("http://image1.com", images.get(0).getUrl());
        assertEquals("desc1", images.get(0).getDescription());
        assertEquals("http://image2.com", images.get(1).getUrl());
        assertNull(images.get(1).getDescription());
    }

    @Test
    void testTavilyRequestLombok() {
        TavilyApiClient.TavilyRequest r1 = TavilyApiClient.TavilyRequest.builder()
                .query("q")
                .apiKey("k")
                .searchDepth("basic")
                .topic("general")
                .days(1)
                .maxResults(5)
                .includeImages(true)
                .includeImageDescriptions(true)
                .includeAnswer(true)
                .includeRawContent(true)
                .includeDomains(Arrays.asList("d1"))
                .excludeDomains(Arrays.asList("d2"))
                .build();
        
        TavilyApiClient.TavilyRequest r2 = TavilyApiClient.TavilyRequest.builder()
                .query("q")
                .apiKey("k")
                .searchDepth("basic")
                .topic("general")
                .days(1)
                .maxResults(5)
                .includeImages(true)
                .includeImageDescriptions(true)
                .includeAnswer(true)
                .includeRawContent(true)
                .includeDomains(Arrays.asList("d1"))
                .excludeDomains(Arrays.asList("d2"))
                .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
    }
}
