package com.fined.mentor.auth.service;

import com.fined.mentor.auth.exception.EmailException;
import kong.unirest.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    private MockedStatic<Unirest> unirestMockedStatic;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "domain", "test-domain");
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");

        unirestMockedStatic = mockStatic(Unirest.class);
    }

    @AfterEach
    void tearDown() {
        unirestMockedStatic.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendActivationEmail_Success() {
        HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
        MultipartBody multipartBody = mock(MultipartBody.class);
        HttpResponse<JsonNode> response = (HttpResponse<JsonNode>) mock(HttpResponse.class);

        unirestMockedStatic.when(() -> Unirest.post(anyString())).thenReturn(postRequest);
        when(postRequest.basicAuth(anyString(), anyString())).thenReturn(postRequest);
        when(postRequest.field(anyString(), anyString())).thenReturn(multipartBody);
        when(multipartBody.field(anyString(), anyString())).thenReturn(multipartBody);
        when(multipartBody.asJson()).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);

        emailService.sendActivationEmail("user@example.com", "testuser", "123456");

        verify(postRequest).field(anyString(), anyString());
        verify(multipartBody, times(3)).field(anyString(), anyString());
        verify(multipartBody).asJson();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendPasswordResetEmail_Success() {
        HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
        MultipartBody multipartBody = mock(MultipartBody.class);
        HttpResponse<JsonNode> response = (HttpResponse<JsonNode>) mock(HttpResponse.class);

        unirestMockedStatic.when(() -> Unirest.post(anyString())).thenReturn(postRequest);
        when(postRequest.basicAuth(anyString(), anyString())).thenReturn(postRequest);
        when(postRequest.field(anyString(), anyString())).thenReturn(multipartBody);
        when(multipartBody.field(anyString(), anyString())).thenReturn(multipartBody);
        when(multipartBody.asJson()).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);

        emailService.sendPasswordResetEmail("user@example.com", "testuser", "123456");

        verify(postRequest).field(anyString(), anyString());
        verify(multipartBody, times(3)).field(anyString(), anyString());
        verify(multipartBody).asJson();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendEmail_Failure_ThrowsEmailException() {
        HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
        MultipartBody multipartBody = mock(MultipartBody.class);
        HttpResponse<JsonNode> response = (HttpResponse<JsonNode>) mock(HttpResponse.class);

        unirestMockedStatic.when(() -> Unirest.post(anyString())).thenReturn(postRequest);
        when(postRequest.basicAuth(anyString(), anyString())).thenReturn(postRequest);
        when(postRequest.field(anyString(), anyString())).thenReturn(multipartBody);
        when(multipartBody.field(anyString(), anyString())).thenReturn(multipartBody);
        when(multipartBody.asJson()).thenReturn(response);
        when(response.isSuccess()).thenReturn(false);
        when(response.getStatus()).thenReturn(400);

        assertThrows(EmailException.class, () -> 
            emailService.sendActivationEmail("user@example.com", "testuser", "123456"));
    }

    @Test
    void sendEmail_Exception_ThrowsEmailException() {
        HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);

        unirestMockedStatic.when(() -> Unirest.post(anyString())).thenReturn(postRequest);
        when(postRequest.basicAuth(anyString(), anyString())).thenReturn(postRequest);
        when(postRequest.field(anyString(), anyString())).thenThrow(new RuntimeException("Connection error"));

        assertThrows(EmailException.class, () -> 
            emailService.sendActivationEmail("user@example.com", "testuser", "123456"));
    }
}
