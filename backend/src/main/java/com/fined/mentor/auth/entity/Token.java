package com.fined.mentor.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tokens")
public class Token {
    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @DBRef
    private User user;

    private TokenType type;

    private Instant createdAt;
    private Instant expiresAt;
    private Instant usedAt;

    public enum TokenType {
        ACTIVATION,
        PASSWORD_RESET,
        EMAIL_CHANGE
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired() && usedAt == null;
    }
}