package com.fined.mentor.auth.service;

import com.fined.mentor.auth.entity.Token;
import com.fined.mentor.auth.entity.User;
import com.fined.mentor.auth.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public Token createActivationToken(User user) {
        // Delete any existing activation tokens for this user
        tokenRepository.deleteByUserAndType(user, Token.TokenType.ACTIVATION);

        String tokenValue = generateOTP();

        Token token = Token.builder()
                .token(tokenValue)
                .user(user)
                .type(Token.TokenType.ACTIVATION)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                .build();

        Token savedToken = tokenRepository.save(token);
        log.debug("Created activation token for user: {}", user.getEmail());

        return savedToken;
    }

    public Token createPasswordResetToken(User user) {
        // Delete any existing password reset tokens for this user
        tokenRepository.deleteByUserAndType(user, Token.TokenType.PASSWORD_RESET);

        String tokenValue = generateOTP();

        Token token = Token.builder()
                .token(tokenValue)
                .user(user)
                .type(Token.TokenType.PASSWORD_RESET)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                .build();

        Token savedToken = tokenRepository.save(token);
        log.debug("Created password reset token for user: {}", user.getEmail());

        return savedToken;
    }

    public Optional<Token> validateToken(String tokenValue, Token.TokenType type) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndType(tokenValue, type);

        if (tokenOpt.isEmpty()) {
            log.warn("Token not found: {}", tokenValue);
            return Optional.empty();
        }

        Token token = tokenOpt.get();

        if (!token.isValid()) {
            log.warn("Token is invalid or expired: {}", tokenValue);
            return Optional.empty();
        }

        return Optional.of(token);
    }

    public void markTokenAsUsed(Token token) {
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
        log.debug("Marked token as used: {}", token.getToken());
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateOTP() {
        // Generate a 6-digit OTP using SecureRandom
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }
}