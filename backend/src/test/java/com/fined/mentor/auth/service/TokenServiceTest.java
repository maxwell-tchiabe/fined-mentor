package com.fined.mentor.auth.service;

import com.fined.mentor.auth.entity.Token;
import com.fined.mentor.auth.entity.User;
import com.fined.mentor.auth.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private User sampleUser;
    private Token sampleToken;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("1")
                .username("testuser")
                .email("test@example.com")
                .build();

        sampleToken = Token.builder()
                .id("token_id_1")
                .token("123456")
                .user(sampleUser)
                .type(Token.TokenType.ACTIVATION)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();
    }

    @Test
    void createActivationToken_Success() {
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Token token = tokenService.createActivationToken(sampleUser);

        assertNotNull(token);
        assertEquals(Token.TokenType.ACTIVATION, token.getType());
        assertEquals(sampleUser, token.getUser());
        assertNotNull(token.getToken());
        assertEquals(6, token.getToken().length());
        
        verify(tokenRepository).deleteByUserAndType(sampleUser, Token.TokenType.ACTIVATION);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void createPasswordResetToken_Success() {
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Token token = tokenService.createPasswordResetToken(sampleUser);

        assertNotNull(token);
        assertEquals(Token.TokenType.PASSWORD_RESET, token.getType());
        assertEquals(sampleUser, token.getUser());
        assertNotNull(token.getToken());
        assertEquals(6, token.getToken().length());

        verify(tokenRepository).deleteByUserAndType(sampleUser, Token.TokenType.PASSWORD_RESET);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void validateToken_ValidToken() {
        when(tokenRepository.findByTokenAndType("123456", Token.TokenType.ACTIVATION))
                .thenReturn(Optional.of(sampleToken));

        Optional<Token> result = tokenService.validateToken("123456", Token.TokenType.ACTIVATION);

        assertTrue(result.isPresent());
        assertEquals(sampleToken, result.get());
    }

    @Test
    void validateToken_NotFound() {
        when(tokenRepository.findByTokenAndType("999999", Token.TokenType.ACTIVATION))
                .thenReturn(Optional.empty());

        Optional<Token> result = tokenService.validateToken("999999", Token.TokenType.ACTIVATION);

        assertFalse(result.isPresent());
    }

    @Test
    void validateToken_Expired() {
        sampleToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(tokenRepository.findByTokenAndType("123456", Token.TokenType.ACTIVATION))
                .thenReturn(Optional.of(sampleToken));

        Optional<Token> result = tokenService.validateToken("123456", Token.TokenType.ACTIVATION);

        assertFalse(result.isPresent());
    }

    @Test
    void validateToken_AlreadyUsed() {
        sampleToken.setUsedAt(Instant.now());
        when(tokenRepository.findByTokenAndType("123456", Token.TokenType.ACTIVATION))
                .thenReturn(Optional.of(sampleToken));

        Optional<Token> result = tokenService.validateToken("123456", Token.TokenType.ACTIVATION);

        assertFalse(result.isPresent());
    }

    @Test
    void markTokenAsUsed_Success() {
        when(tokenRepository.save(any(Token.class))).thenReturn(sampleToken);

        tokenService.markTokenAsUsed(sampleToken);

        assertNotNull(sampleToken.getUsedAt());
        verify(tokenRepository).save(sampleToken);
    }
}
